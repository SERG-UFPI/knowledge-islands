package br.com.gitanalyzer.extractors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import br.com.gitanalyzer.model.Commit;
import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.ProjectVersion;
import br.com.gitanalyzer.model.entity.QualityMeasures;
import br.com.gitanalyzer.utils.Constants;
import br.com.gitanalyzer.utils.ContributorUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectVersionExtractor {

	private FileExtractor fileExtractor = new FileExtractor();
	private CommitExtractor commitExtractor = new CommitExtractor();
	private ContributorUtils contributorUtils = new ContributorUtils();
	private CkMeasuresExtractor ckMeasuresExtractor = new CkMeasuresExtractor();

	public ProjectVersion extractProjectVersion(String projectPath, String projectName) throws IOException {
		log.info("EXTRACTING PROJECT VERSION OF "+projectName);
		QualityMeasures qualityMeasures = ckMeasuresExtractor.extract(projectPath);
		if(projectPath.substring(projectPath.length() -1).equals("/") == false) {
			projectPath = projectPath+"/";
		}
		int numberAllFiles = fileExtractor.extractSizeAllFiles(projectPath, Constants.allFilesFileName);
		List<File> files = fileExtractor.extractFilesFromClocFile(projectPath, Constants.clocFileName, projectName);
		int numberAnalysedFiles = files.size();
		fileExtractor.getRenamesFiles(projectPath, files);
		List<Commit> commits = commitExtractor.extractCommitsFromLogFiles(projectPath);
		Date dateVersion = commits.get(0).getDate();
		String versionId = commits.get(0).getExternalId();
		int numberAllCommits = commits.size();
		commits = commitExtractor.extractCommitsFiles(projectPath, commits, files);
		commits.removeIf(c -> c.getCommitFiles().size() == 0);
		commits = commitExtractor.extractCommitsFileAndDiffsOfCommits(projectPath, commits, files);
		int numberAnalysedCommits = commits.size();
		List<Contributor> contributors = extractContributorFromCommits(commits);
		contributors = setAlias(contributors, projectName);
		contributors = contributors.stream().filter(c -> c.getEmail() != null && c.getName() != null).toList();
		int numberAnalysedDevs = contributors.size();
		Collections.sort(commits, new Comparator<Commit>() {
			@Override
			public int compare(Commit c1, Commit c2) {
				return c2.getDate().compareTo(c1.getDate());
			}
		});
		ProjectVersion projectVersion = new ProjectVersion(numberAnalysedDevs, 
				numberAllFiles, numberAnalysedFiles, numberAllCommits, numberAnalysedCommits, 
				dateVersion, versionId, contributorUtils.setActiveContributors(contributors, commits), qualityMeasures);
		projectVersion.setCommits(commits);
		projectVersion.setFiles(files);
		return projectVersion;
	}

	public ProjectVersion extractProjectVersionFiltering(String projectPath) {
		int numberAllFiles = fileExtractor.extractSizeAllFiles(projectPath, Constants.allFilesFileName);
		List<Commit> commits = commitExtractor.extractCommitsFromLogFiles(projectPath);
		int numberAllCommits = commits.size();
		List<Contributor> contributors = extractContributorFromCommits(commits);
		contributors = setAlias(contributors, null);
		int numberAnalysedDevs = contributors.size();
		ProjectVersion projectVersion = ProjectVersion.builder().numberAllCommits(numberAllCommits)
				.numberAllFiles(numberAllFiles).numberAnalysedDevs(numberAnalysedDevs).build();
		return projectVersion;
	}

	private List<Contributor> extractContributorFromCommits(List<Commit> commits){
		List<Contributor> contributors = new ArrayList<Contributor>();
		forCommit: for (Commit commit : commits) {
			Contributor contributor = commit.getAuthor();
			for (Contributor contributor2 : contributors) {
				if (contributor2.equals(contributor)) {
					continue forCommit;
				}
			}
			contributors.add(contributor);
		}
		return contributors;
	}

	private List<Contributor> setAlias(List<Contributor> contributors, String projectName){
		List<Contributor> contributorsAliases = new ArrayList<Contributor>();
		forContributors:for (int i = 0; i < contributors.size(); i++) {
			for (Contributor contributorAlias : contributorsAliases) {
				if(contributorAlias.getAlias() != null && contributorAlias.getAlias().size() > 0) {
					for (Contributor contributorAliasAux : contributorAlias.getAlias()) {
						if (contributors.get(i).equals(contributorAliasAux)) {
							continue forContributors;
						}
					}
				}
			}
			for(int j = i+1; j < contributors.size(); j++) {
				boolean alias = false;
				if(contributors.get(j).getEmail().equals(contributors.get(i).getEmail())) {
					alias = true;
				}
				//					else if(projectName != null && projectName.toUpperCase().equals("IHEALTH") && 
				//							((contributorAux.getName().toUpperCase().contains("CLEITON") && contributor.getName().toUpperCase().contains("CLEITON")) 
				//									|| (contributorAux.getName().toUpperCase().contains("JARDIEL") && contributor.getName().toUpperCase().contains("JARDIEL"))
				//									|| (contributorAux.getName().toUpperCase().contains("THASCIANO") && contributor.getName().toUpperCase().contains("THASCIANO"))
				//									|| ((contributorAux.getEmail().equals("lucas@infoway-pi.com.br") && contributor.getEmail().equals("lucas@91d758c7-b022-4e42-997a-adfec6647064")) || 
				//											(contributor.getEmail().equals("lucas@infoway-pi.com.br") && contributorAux.getEmail().equals("lucas@91d758c7-b022-4e42-997a-adfec6647064"))))) {
				//						alias.add(contributorAux);
				//					}
				//					else if(projectName != null && projectName.toUpperCase().equals("CONSULTA-CADASTRO-API")
				//							&& (contributorAux.getName().toUpperCase().contains("MAYKON") && contributor.getName().toUpperCase().contains("MAYKON"))) {
				//						alias.add(contributorAux);
				//					}
				else{
					if(contributors.get(j).getName() != null) {
						int distance = StringUtils.getLevenshteinDistance(contributors.get(i).getName().toUpperCase()
								, contributors.get(j).getName().toUpperCase());
						//								if (nome.equals(contributor.getName().toUpperCase()) || 
						//										(distance/(double)contributor.getName().length() < 0.1)) {
						//									alias.add(contributorAux);
						//								}
						if (distance <= 1) {
							alias = true;
						}
					}
				}
				if(alias == true) {
					if(contributors.get(i).getAlias() == null) {
						contributors.get(i).setAlias(new HashSet<Contributor>());
					}
					contributors.get(i).getAlias().add(contributors.get(j));
				}
			}
			if(contributors.get(i).getAlias() != null && contributors.get(i).getAlias().size() > 0) {
				for (Contributor alias : contributors.get(i).getAlias()) {
					for (Contributor contributorAlias : contributorsAliases) {
						if(contributorAlias.getAlias() != null && contributorAlias.getAlias().size() > 0) {
							for (Contributor contributorAliasAux : contributorAlias.getAlias()) {
								if (alias.equals(contributorAliasAux)) {
									List<Contributor> aux = new ArrayList<Contributor>();
									aux.addAll(contributors.get(i).getAlias());
									aux.add(contributors.get(i));
									contributors.get(i).getAlias().clear();
									contributorAlias.getAlias().addAll(aux);
									continue forContributors;
								}
							}
						}
					}
				}
			}
			contributorsAliases.add(contributors.get(i));
		}
		return contributorsAliases;
	}

	private List<Commit> filterCommitsByFilesTouched(String projectName, List<Commit> commits) {
		if(projectName != null && projectName.toUpperCase().equals("IHEALTH")) {
			return commits.stream().filter(c -> c.getCommitFiles().size() < 90).collect(Collectors.toList());
		}
		return commits;
	}

}
