package br.com.gitanalyzer.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.dto.FilteringProjectsDTO;
import br.com.gitanalyzer.enums.FilteredEnum;
import br.com.gitanalyzer.enums.OperationType;
import br.com.gitanalyzer.enums.TimeIntervalTypeEnum;
import br.com.gitanalyzer.extractors.CommitExtractor;
import br.com.gitanalyzer.extractors.FileExtractor;
import br.com.gitanalyzer.extractors.ProjectVersionExtractor;
import br.com.gitanalyzer.model.Commit;
import br.com.gitanalyzer.model.CommitFile;
import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.Project;
import br.com.gitanalyzer.model.entity.ProjectVersion;
import br.com.gitanalyzer.repository.ProjectRepository;
import br.com.gitanalyzer.utils.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FilterProjectService {

	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private ProjectService projectService;
	
	public void filterEcoSpring() throws URISyntaxException, IOException, InterruptedException {
		List<Project> projects = projectRepository.findAll();
		List<Project> projectFilteredLanguage = projects.stream().filter(p -> p.getMainLanguage() == null ||
				p.getMainLanguage().equals("Java") == false).toList();
		for (Project project : projectFilteredLanguage) {
			project.setFiltered(true);
			project.setFilteredReason(FilteredEnum.NOT_THE_ANALYZED_LANGUAGE);
		}
		List<String> notProjectSoftwareNames = new ArrayList<>();
		notProjectSoftwareNames.add("spring-projects/spring-data-examples");
		notProjectSoftwareNames.add("spring-projects/spring-integration-samples");
		notProjectSoftwareNames.add("spring-projects/spring-security-samples");
		List<Project> projectFilteredNames = projects.stream().filter(p -> notProjectSoftwareNames.contains(p.getFullName())).toList();
		for (Project project : projectFilteredNames) {
			project.setFiltered(true);
			project.setFilteredReason(FilteredEnum.NOT_SOFTWARE_PROJECT);
		}
		projectRepository.saveAll(projectFilteredNames);
		projectRepository.saveAll(projectFilteredLanguage);
	}

	public void filter(FilteringProjectsDTO form) throws URISyntaxException, IOException, InterruptedException {
		ProjectVersionExtractor projectVersionExtractor = new ProjectVersionExtractor();
		List<ProjectVersion> versions = new ArrayList<ProjectVersion>();
		java.io.File dir = new java.io.File(form.getFolderPath());
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				Project project = projectService.returnProjectByPath(projectPath);
				log.info("EXTRACTING DATA FROM "+project.getName());
				ProjectVersion version = projectVersionExtractor.extractProjectVersionFiltering(projectPath);
				version.setProject(project);
				versions.add(version);
				log.info("EXTRACTION FINISHED");

			}
		}
		List<Project> projectsFiltered = new ArrayList<Project>();
		Map<String, List<ProjectVersion>> versionMap = versions.stream().collect(Collectors.groupingBy(ProjectVersion::getProjectLanguage));
		for(var entry: versionMap.entrySet()) {
			projectsFiltered.addAll(filterProjectBySize(entry.getValue()));
		}
		//		for(ProjectVersion version: versions) {
		//			if(projectsFiltered.stream()
		//					.anyMatch(p -> p.getId().equals(version.getProject().getId())) == false) {
		//				log.info("FILTERING BY COMMITS-FILES "+version.getProject().getName());
		//				if(filterProjectByCommits(version)) {
		//					version.getProject().setFilteredReason(FilteredEnum.HISTORY_MIGRATION);
		//					projectsFiltered.add(version.getProject());
		//				}
		//			}
		//		}
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int calendarType = form.getIntervalType().equals(TimeIntervalTypeEnum.MONTH)?Calendar.MONTH:Calendar.YEAR;
		c.add(calendarType, -form.getInterval());
		List<Project> projects = versions.stream().map(v -> v.getProject()).toList();
		for (Project project : projects) {
			if(project.getFirstCommitDate().after(c.getTime())) {
				projectsFiltered.add(project);
				project.setFilteredReason(FilteredEnum.PROJECT_AGE);
			}
		}
		for (Project project : projectsFiltered) {
			project.setFiltered(true);
			projectRepository.save(project);
		}
	}

	private boolean filterProjectByCommits(ProjectVersion version) {
		FileExtractor fileExtractor = new FileExtractor();
		CommitExtractor commitExtractor = new CommitExtractor();
		List<File> files = fileExtractor.extractFileList(version.getProject().getCurrentPath(), Constants.linguistFileName, null);
		fileExtractor.getRenamesFiles(version.getProject().getCurrentPath(), files);
		List<Commit> commits = commitExtractor.extractCommitsFromLogFiles(version.getProject().getCurrentPath());
		commits = getFirst20Commits(commits);
		commits = commitExtractor.extractCommitsFiles(version.getProject().getCurrentPath(), commits, files);
		int numberOfFiles = version.getNumberAnalysedFiles();
		List<File> addedFiles = new ArrayList<File>();
		for(Commit commit: commits) {
			for (CommitFile commitFile : commit.getCommitFiles()) {
				if(commitFile.getOperation().equals(OperationType.ADD)) {
					addedFiles.add(commitFile.getFile());
				}
			}
		}
		int numberOfCurrentFilesAdded = 0;
		for (File file : addedFiles) {
			if(files.stream().anyMatch(f -> f.isFile(file.getPath()))) {
				numberOfCurrentFilesAdded++;
			}
		}
		if(numberOfCurrentFilesAdded > (numberOfFiles*0.5)) {
			return true;
		}
		return false;
	}

	private List<Commit> getFirst20Commits(List<Commit> commits) {
		List<Commit> firstCommits = new ArrayList<Commit>();
		Collections.reverse(commits);
		for(int i = 0; i < 20; i++) {
			firstCommits.add(commits.get(i));
		}
		return firstCommits;
	}

	private List<Project> filterProjectBySize(List<ProjectVersion> versions) {
		List<Double> devs = versions.stream().map(v -> Double.valueOf(v.getNumberAnalysedDevs())).toList();
		List<Double> commits = versions.stream().map(v -> Double.valueOf(v.getNumberAllCommits())).toList();
		List<Double> files = versions.stream().map(v -> Double.valueOf(v.getNumberAllFiles())).toList();
		double[] devsArray = new double[devs.size()];
		int i=0;
		for(Double dev: devs) {
			devsArray[i] = dev;
			i++;
		}
		double[] commitsArray = new double[commits.size()];
		i=0;
		for(Double commit: commits) {
			commitsArray[i] = commit;
			i++;
		}
		double[] filesArray = new double[files.size()];
		i=0;
		for(Double file: files) {
			filesArray[i] = file;
			i++;
		}

		Percentile p = new Percentile();
		double firstQDevs = p.evaluate(devsArray, 25);
		double firstQCommits = p.evaluate(commitsArray, 25);
		double firstQFiles = p.evaluate(filesArray, 25);
		Set<Project> projects = new HashSet<Project>();
		projects.addAll(versions.stream().filter(v -> v.getNumberAnalysedDevs() < firstQDevs).map(v -> v.getProject()).toList());
		projects.addAll(versions.stream().filter(v -> v.getNumberAllCommits() < firstQCommits).map(v -> v.getProject()).toList());
		projects.addAll(versions.stream().filter(v -> v.getNumberAllFiles() < firstQFiles).map(v -> v.getProject()).toList());
		projects.stream().forEach(pr -> pr.setFilteredReason(FilteredEnum.SIZE));
		return new ArrayList<>(projects);
	}

}
