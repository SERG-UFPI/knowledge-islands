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

@Service
public class FilterProjectService {

	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private ProjectService projectService;

	public void filterEcoSpring() throws URISyntaxException, IOException, InterruptedException {
		List<Project> projects = projectRepository.findAll();
		filterProjectsByAge(projects);
		for (Project project : projects) {
			if(project.getMainLanguage() == null ||
					project.getMainLanguage().equals("Java") == false) {
				project.setFiltered(true);
				project.setFilteredReason(FilteredEnum.NOT_THE_ANALYZED_LANGUAGE);
				projectRepository.save(project);
			}
		}
		filterNotSoftwareProjects(projects);
		filterProjectsByInactive(projects);
		projectRepository.saveAll(projects);
	}

	public void filter(FilteringProjectsDTO form) throws URISyntaxException, IOException, InterruptedException {
		ProjectVersionExtractor projectVersionExtractor = new ProjectVersionExtractor();
		List<ProjectVersion> versions = new ArrayList<ProjectVersion>();
		java.io.File dir = new java.io.File(form.getFolderPath());
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				Project project = projectService.returnProjectByPath(projectPath);
				ProjectVersion version = projectVersionExtractor.extractProjectVersionFiltering(projectPath);
				version.setProject(project);
				versions.add(version);
			}
		}
		Map<String, List<ProjectVersion>> versionMap = versions.stream().collect(Collectors.groupingBy(ProjectVersion::getProjectLanguage));
		for(var entry: versionMap.entrySet()) {
			filterProjectBySize(entry.getValue());
		}
		for(ProjectVersion version: versions) {
			if(version.getProject().isFiltered() == false) {
				if(filterProjectByCommits(version)) {
					version.getProject().setFiltered(true);
					version.getProject().setFilteredReason(FilteredEnum.HISTORY_MIGRATION);
					projectRepository.save(version.getProject());
				}
			}
		}
		List<Project> projects = versions.stream().map(v -> v.getProject()).toList();
		filterProjectsByAge(projects);
		for (Project project : projects) {
			if(project.getMainLanguage() == null) {
				project.setFiltered(true);
				project.setFilteredReason(FilteredEnum.NOT_THE_ANALYZED_LANGUAGE);
				projectRepository.save(project);
			}
		}
		filterNotSoftwareProjects(projects);
		filterProjectsByInactive(projects);
	}

	public void filterNotSoftwareProjects(List<Project> projects) {
		List<String> notProjectSoftwareNames = Constants.projectsToRemoveInFiltering();
		for (Project project : projects) {
			if(notProjectSoftwareNames.contains(project.getFullName()) && project.isFiltered() == false) {
				project.setFiltered(true);
				project.setFilteredReason(FilteredEnum.NOT_SOFTWARE_PROJECT);
				projectRepository.save(project);
			}
		}
	}

	public void filterProjectsByInactive(List<Project> projects) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int calendarType = Calendar.YEAR;
		c.add(calendarType, -Constants.intervalYearsProjectConsideredInactivate);
		for (Project project : projects) {
			if(project.getDownloadVersionDate() != null && 
					project.getDownloadVersionDate().before(c.getTime()) && project.isFiltered() == false) {
				project.setFiltered(true);
				project.setFilteredReason(FilteredEnum.INACTIVE_PROJECT);
				projectRepository.save(project);
			}
		}
	}

	public void filterProjectsByAge(List<Project> projects) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int calendarType = Calendar.YEAR;
		c.add(calendarType, -Constants.intervalYearsProjectAgeFilter);
		for (Project project : projects) {
			if(project.getFirstCommitDate() != null && 
					project.getFirstCommitDate().after(c.getTime()) && project.isFiltered() == false) {
				project.setFiltered(true);
				project.setFilteredReason(FilteredEnum.PROJECT_AGE);
				projectRepository.save(project);
			}
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

	private void filterProjectBySize(List<ProjectVersion> versions) {
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
		projects.stream().forEach(pr -> pr.setFiltered(true));
		for (Project project : projects) {
			projectRepository.save(project);
		}
	}

}
