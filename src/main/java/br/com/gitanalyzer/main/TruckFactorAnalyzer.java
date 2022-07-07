package br.com.gitanalyzer.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import br.com.gitanalyzer.enums.KnowledgeMetric;
import br.com.gitanalyzer.enums.OperationType;
import br.com.gitanalyzer.extractors.CommitExtractor;
import br.com.gitanalyzer.extractors.FileExtractor;
import br.com.gitanalyzer.extractors.ProjectExtractor;
import br.com.gitanalyzer.model.AuthorFile;
import br.com.gitanalyzer.model.Commit;
import br.com.gitanalyzer.model.CommitFile;
import br.com.gitanalyzer.model.Contributor;
import br.com.gitanalyzer.model.File;
import br.com.gitanalyzer.model.Project;
import br.com.gitanalyzer.utils.Constants;
import br.com.gitanalyzer.utils.DoaUtils;
import br.com.gitanalyzer.utils.DoeUtils;
import br.com.gitanalyzer.utils.FileUtils;

public class TruckFactorAnalyzer {

	private FileUtils fileUtils = new FileUtils();
	private DoeUtils doeUtils = new DoeUtils();
	private DoaUtils doaUtils = new DoaUtils();

	private static List<String> invalidsProjects = new ArrayList<String>(Arrays.asList("sass", 
			"ionic", "cucumber"));

	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

	public static void main(String[] args) {

		TruckFactorAnalyzer truckFactorAnalyzer = new TruckFactorAnalyzer();
		String pathToDir = args[0];
		try {
			truckFactorAnalyzer.directoriesTruckFactorAnalyzes(pathToDir);
		} catch (NoHeadException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		//truckFactorAnalyzer.analysesIhealth();
	}

	private void analysesIhealth() {
		TruckFactorAnalyzer truckFactorAnalyzer = new TruckFactorAnalyzer();
		try {
			truckFactorAnalyzer.projectTruckFactorAnalyzes("/home/otavio/Desktop/GitAnalyzer/projetos/ihealth/", 
					"/home/otavio/Desktop/GitAnalyzer/projetos/", KnowledgeMetric.DOE);
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
	}

	protected void projectTruckFactorAnalyzes(String projectPath, String resultFilesDirectory, KnowledgeMetric knowledgeMetric) throws IOException, NoHeadException, GitAPIException {

		FileInputStream fstream = new FileInputStream(resultFilesDirectory+Constants.truckFactorResultFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		CommitExtractor commitExtractor = new CommitExtractor();

		int numberAllDevs, numberAnalysedDevs, numberAnalysedDevsAlias, 
		numberAllFiles, numberAnalysedFiles, numberAllCommits, numberAnalysedCommits, truckfactor;
		String projectName;

		ProjectExtractor projectExtractor = new ProjectExtractor();
		projectName = projectExtractor.extractProjectName(projectPath);
		if (invalidsProjects.contains(projectName) == false) {

			boolean present = false;
			String strLine;

			while ((strLine = br.readLine()) != null) {
				String[] line = strLine.split(";");
				String name = line[8];
				if (name.equals(projectName)) {
					present = true;
					break;
				}
			}

			if (present == false) {
				Project project = new Project(projectName);
				Git git = null;
				Repository repository;
				try {
					git = Git.open(new java.io.File(projectPath));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				repository = git.getRepository();

				FileExtractor fileExtractor = new FileExtractor(project);
				System.out.println("EXTRACTING DATA FROM "+projectPath);
				List<String> allFiles = fileUtils.currentFiles(repository);
				numberAllFiles = allFiles.size();
				List<File> files = fileExtractor.extractFromFileList(projectPath, Constants.linguistFileName, 
						Constants.clocFileName, repository);
				numberAnalysedFiles = files.size();
				List<RevCommit> allCommits = extractAllCommits(git);
				numberAllCommits = allCommits.size();
				List<Contributor> allContributors = extractAuthor(allCommits);
				numberAllDevs = allContributors.size();
				List<Commit> commits = commitExtractor.extractCommitsWithoutPersistence(files, git, repository);
				numberAnalysedCommits = commits.size();
				List<Contributor> contributors = extractContributorFromCommits(commits);
				numberAnalysedDevs = contributors.size();
				contributors = setAlias(contributors);
				numberAnalysedDevsAlias = contributors.size();
				List<AuthorFile> authorFiles = new ArrayList<AuthorFile>();
				for(Contributor contributor: contributors) {
					for (File file : files) {
						boolean existsContributorFile = existsContributorFile(contributor, file, commits);
						if (existsContributorFile) {
							int firstAuthor = firstAuthorContributorFile(contributor, file, commits);
							if (knowledgeMetric.equals(KnowledgeMetric.DOE)) {
								int adds = linesAddedContributorFile(contributor, file, commits);
								int numDays = numDaysContributorFile(contributor, file, commits);
								int fileSize = file.getFileSize();
								double doe = doeUtils.getDOE(adds, firstAuthor, numDays, fileSize);
								authorFiles.add(new AuthorFile(contributor, file, doe));
							}else {
								int dl = numberOfCommitsContributorFile(contributor, file, commits);
								int ac = numberOfCommitsOtherDevsContributorFile(contributor, file, commits);
								double doa = doaUtils.getDOA(firstAuthor, dl, ac);
								authorFiles.add(new AuthorFile(contributor, doa, file));
							}
						}
					}
				}
				setNumberAuthor(contributors, authorFiles, files, knowledgeMetric);
				Collections.sort(contributors, new Comparator<Contributor>() {
					@Override
					public int compare(Contributor c1, Contributor c2) {
						return Integer.compare(c2.getNumberFilesAuthor(), c1.getNumberFilesAuthor());
					}
				});
				contributors.removeIf(contributor -> contributor.getNumberFilesAuthor() == 0);
				List<Contributor> topContributors = new ArrayList<Contributor>();
				int tf = 0;
				while(contributors.isEmpty() == false) {
					double covarage = getCoverage(authorFiles, contributors, files, knowledgeMetric);
					if(covarage < 0.5) 
						break;
					topContributors.add(contributors.get(0));
					contributors.remove(0);
					tf = tf+1;
				}

				String dateLastCommit = simpleDateFormat.format(commits.get(0).getDate());

				FileWriter fileWriterDevs = new FileWriter(resultFilesDirectory+Constants.developersProjectFileName, true);
				BufferedWriter bwDevs = new BufferedWriter(fileWriterDevs);
				for (Contributor contributor : topContributors) {
					TruckFactorDevelopersVO developersVO = new TruckFactorDevelopersVO(contributor.getName(), contributor.getEmail(), projectName, dateLastCommit, knowledgeMetric.getName());
					bwDevs.write(developersVO.toString());
					bwDevs.newLine();
				}
				bwDevs.close();

				truckfactor = tf;
				TruckFactorVO truckFactorVO = new TruckFactorVO(numberAllDevs, numberAnalysedDevs, 
						numberAnalysedDevsAlias, numberAllFiles, numberAnalysedFiles, 
						numberAllCommits, numberAnalysedCommits, truckfactor, projectName, 
						dateLastCommit, knowledgeMetric.getName()); 

				FileWriter fileWriter = new FileWriter(resultFilesDirectory+Constants.truckFactorResultFile, true);
				BufferedWriter bw = new BufferedWriter(fileWriter);
				bw.write(truckFactorVO.toString());
				bw.newLine();
				bw.close();
			}
		}
		br.close();
	}

	protected int numberOfCommitsOtherDevsContributorFile(Contributor contributor, 
			File file, List<Commit> commits) {
		int numerCommits = 0;
		List<Contributor> contributors = new ArrayList<Contributor>();
		contributors.add(contributor);
		contributors.addAll(contributor.getAlias());
		for (Commit commit : commits) {
			boolean present = false;
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					present = true;
					break;
				}
			}
			if (present == false) {
				for (CommitFile commitFile: commit.getCommitFiles()) {
					if (commitFile.getFile().getPath().equals(file.getPath())) {
						numerCommits++;
						break;
					}
				}
			}
		}
		return numerCommits;
	}

	protected int numberOfCommitsContributorFile(Contributor contributor, 
			File file, List<Commit> commits) {
		int numerCommits = 0;
		List<Contributor> contributors = new ArrayList<Contributor>();
		contributors.add(contributor);
		contributors.addAll(contributor.getAlias());
		for (Commit commit : commits) {
			boolean present = false;
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					present = true;
					break;
				}
			}
			if (present == true) {
				for (CommitFile commitFile: commit.getCommitFiles()) {
					if (commitFile.getFile().getPath().equals(file.getPath())) {
						numerCommits++;
						break;
					}
				}
			}
		}
		return numerCommits;
	}


	protected void directoriesTruckFactorAnalyzes(String pathToDirectories) throws IOException, NoHeadException, GitAPIException{

		java.io.File dir = new java.io.File(pathToDirectories);
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				projectTruckFactorAnalyzes(projectPath, pathToDirectories, KnowledgeMetric.DOE);
			}
		}
	}

	protected List<Contributor> extractAuthor(List<RevCommit> commitsList) {
		List<Contributor> contributors = new ArrayList<Contributor>();
		for (RevCommit jgitCommit: commitsList) {
			String nome = null, email = null;
			if (jgitCommit.getAuthorIdent() != null) {
				if (jgitCommit.getAuthorIdent().getEmailAddress() != null) {
					email = jgitCommit.getAuthorIdent().getEmailAddress();
				}else {
					email = jgitCommit.getCommitterIdent().getEmailAddress();
				}
				if (jgitCommit.getAuthorIdent().getName() != null) {
					nome = jgitCommit.getAuthorIdent().getName();
				}else {
					nome = jgitCommit.getCommitterIdent().getName();
				}
			}else {
				email = jgitCommit.getCommitterIdent().getEmailAddress();
				nome = jgitCommit.getCommitterIdent().getName();
			}
			Contributor author = new Contributor(nome, email);
			boolean present = false;
			for (Contributor contributor : contributors) {
				if (contributor.equals(author)) {
					present = true;
				}
			}
			if (present == false) {
				contributors.add(author);
			}
		}
		return contributors;
	}

	protected List<RevCommit> extractAllCommits(Git git){
		try {
			Iterable<RevCommit> commitsIterable = git.log().call();
			List<RevCommit> commitsList = new ArrayList<RevCommit>();
			commitsIterable.forEach(commitsList::add);
			return commitsList;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	protected double getCoverage(List<AuthorFile> authorsFiles, List<Contributor> contributors, 
			List<File> files, KnowledgeMetric knowledgeMetric) {
		int fileSize = files.size();
		int numberFilesCovarage = 0;
		forFiles:for(File file: files) {
			List<AuthorFile> authorsFilesAux = authorsFiles.stream().
					filter(authorFile -> authorFile.getFile().getPath().equals(file.getPath())).collect(Collectors.toList());
			if (authorsFilesAux != null && authorsFilesAux.size() > 0) {
				List<Contributor> maintainers = new ArrayList<Contributor>();
				AuthorFile max = authorsFilesAux.stream().max(
						Comparator.comparing(knowledgeMetric.equals(KnowledgeMetric.DOE)?AuthorFile::getDoe:AuthorFile::getDoa)).get();
				for (AuthorFile authorFile : authorsFilesAux) {
					double normalized = 0;
					if (knowledgeMetric.equals(KnowledgeMetric.DOE)) {
						normalized = authorFile.getDoe()/max.getDoe();
						if (normalized > Constants.normalizedThresholdMantainerDOE) {
							maintainers.add(authorFile.getAuthor());
						}
					}else if(knowledgeMetric.equals(KnowledgeMetric.DOA)){
						normalized = authorFile.getDoa()/max.getDoa();
						if (normalized > Constants.normalizedThresholdMantainerDOA && 
								authorFile.getDoa() > Constants.thresholdMantainerDOA) {
							maintainers.add(authorFile.getAuthor());
						}
					}
				}
				if (maintainers.size() > 0) {
					for (Contributor contributor : contributors) {
						for (Contributor maintainer : maintainers) {
							if (maintainer.equals(contributor)) {
								numberFilesCovarage++;
								continue forFiles;
							}
						}
					}
				}
			}
		}
		double coverage = (double)numberFilesCovarage/(double)fileSize;
		return coverage; 
	}

	protected void setNumberAuthor(List<Contributor> contributors, 
			List<AuthorFile> authorsFiles, List<File> files, KnowledgeMetric knowledgeMetric) {
		for (File file: files) {
			List<AuthorFile> authorsFilesAux = authorsFiles.stream().
					filter(authorFile -> authorFile.getFile().getPath().equals(file.getPath())).collect(Collectors.toList());
			if (authorsFilesAux != null && authorsFilesAux.size() > 0) {
				List<Contributor> maintainers = new ArrayList<Contributor>();
				AuthorFile max = authorsFilesAux.stream().max(
						Comparator.comparing(knowledgeMetric.equals(KnowledgeMetric.DOE)?AuthorFile::getDoe:AuthorFile::getDoa)).get();
				for (AuthorFile authorFile : authorsFilesAux) {
					double normalized = 0;
					if (knowledgeMetric.equals(KnowledgeMetric.DOE)) {
						normalized = authorFile.getDoe()/max.getDoe();
						if (normalized > Constants.normalizedThresholdMantainerDOE) {
							maintainers.add(authorFile.getAuthor());
						}
					}else if(knowledgeMetric.equals(KnowledgeMetric.DOA)){
						normalized = authorFile.getDoa()/max.getDoa();
						if (normalized > Constants.normalizedThresholdMantainerDOA && 
								authorFile.getDoa() > Constants.thresholdMantainerDOA) {
							maintainers.add(authorFile.getAuthor());
						}
					}
				}
				for (Contributor maintainer : maintainers) {
					for (Contributor contributor: contributors) {
						if (maintainer.equals(contributor)) {
							contributor.setNumberFilesAuthor(contributor.getNumberFilesAuthor()+1);
						}
					}
				}
			}
		}
	}

	protected boolean existsContributorFile(Contributor contributor, File file, List<Commit> commits) {
		for (Commit commit : commits) {
			boolean present = false;
			List<Contributor> contributors = new ArrayList<Contributor>();
			contributors.add(contributor);
			contributors.addAll(contributor.getAlias());
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					present = true;
					break;
				}
			}
			if (present == true) {
				for (CommitFile commitFile: commit.getCommitFiles()) {
					if (commitFile.getFile().getPath().equals(file.getPath())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	protected int numDaysContributorFile(Contributor contributor, File file, List<Commit> commits) {
		Date currentDate = commits.get(0).getDate();
		List<Contributor> contributors = new ArrayList<Contributor>();
		contributors.add(contributor);
		contributors.addAll(contributor.getAlias());
		for (Commit commit : commits) {
			boolean present = false;
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					present = true;
					break;
				}
			}
			if (present == true) {
				Date dateLastCommit = null;
				for (CommitFile commitFile: commit.getCommitFiles()) {
					if (commitFile.getFile().getPath().equals(file.getPath())) {
						dateLastCommit = commit.getDate();
						break;
					}
				}
				if (dateLastCommit != null) {
					long diff = currentDate.getTime() - dateLastCommit.getTime();
					int diffDays = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
					return diffDays;
				}
			}
		}
		return 0;
	}

	protected int firstAuthorContributorFile(Contributor contributor, File file, List<Commit> commits) {
		List<Contributor> contributors = new ArrayList<Contributor>();
		contributors.add(contributor);
		contributors.addAll(contributor.getAlias());
		for (Commit commit : commits) {
			boolean present = false;
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					present = true;
					break;
				}
			}
			if (present == true) {
				for (CommitFile commitFile: commit.getCommitFiles()) {
					if (commitFile.getFile().getPath().equals(file.getPath()) && commitFile.getOperation().equals(OperationType.ADD)) {
						return 1;
					}
				}
			}
		}
		return 0;
	}

	protected int linesAddedContributorFile(Contributor contributor, 
			File file, List<Commit> commits) {
		int adds = 0;
		List<Contributor> contributors = new ArrayList<Contributor>();
		contributors.add(contributor);
		contributors.addAll(contributor.getAlias());
		for (Commit commit : commits) {
			boolean present = false;
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					present = true;
					break;
				}
			}
			if (present == true) {
				for (CommitFile commitFile: commit.getCommitFiles()) {
					if (commitFile.getFile().getPath().equals(file.getPath())) {
						adds = commitFile.getAdds() + adds;
						break;
					}
				}
			}
		}
		return adds;
	}

	protected List<Contributor> extractContributorFromCommits(List<Commit> commits){
		List<Contributor> contributors = new ArrayList<Contributor>();
		for (Commit commit : commits) {
			Contributor contributor = commit.getAuthor();
			boolean present = false;
			for (Contributor contributor2 : contributors) {
				if (contributor2.equals(contributor)) {
					present = true;
				}
			}
			if (present == false) {
				contributors.add(contributor);
			}
		}
		return contributors;
	}

	protected List<Contributor> setAlias(List<Contributor> contributors){
		List<Contributor> contributorsAliases = new ArrayList<Contributor>();
		for (Contributor contributor : contributors) {
			boolean present = false;
			for (Contributor contributorAlias : contributorsAliases) {
				List<Contributor> contributorsAliasesAux = new ArrayList<Contributor>();
				contributorsAliasesAux.add(contributorAlias);
				contributorsAliasesAux.addAll(contributorAlias.getAlias());
				for (Contributor contributorAliasAux : contributorsAliasesAux) {
					if (contributor.equals(contributorAliasAux)) {
						present = true;
					}
				}
			}
			if (present == false) {
				Set<Contributor> alias = new HashSet<Contributor>();
				for(Contributor contributorAux: contributors) {
					if(contributorAux.equals(contributor) == false) {
						if(contributorAux.getEmail().equals(contributor.getEmail())) {
							alias.add(contributorAux);
						}else if(contributorAux.getName().toUpperCase().contains("JARDIEL")
								&& contributor.getName().toUpperCase().contains("JARDIEL")) {
							alias.add(contributorAux);
						}
						else{
							String nome = contributorAux.getName().toUpperCase();
							if(nome != null) {
								int distancia = StringUtils.getLevenshteinDistance(contributor.getName().toUpperCase(), nome);
								if (nome.equals(contributor.getName().toUpperCase()) || 
										(distancia/(double)contributor.getName().length() < 0.1)) {
									alias.add(contributorAux);
								}
							}
						}
					}
				}
				contributor.setAlias(alias);
				contributorsAliases.add(contributor);
			}
		}
		return contributorsAliases;
	}
}
