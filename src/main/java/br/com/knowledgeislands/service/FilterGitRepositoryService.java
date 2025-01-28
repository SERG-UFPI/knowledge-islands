package br.com.knowledgeislands.service;

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
import org.springframework.transaction.annotation.Transactional;

import br.com.knowledgeislands.dto.FilteringProjectsDTO;
import br.com.knowledgeislands.model.entity.Commit;
import br.com.knowledgeislands.model.entity.CommitFile;
import br.com.knowledgeislands.model.entity.File;
import br.com.knowledgeislands.model.entity.GitRepository;
import br.com.knowledgeislands.model.entity.GitRepositoryVersion;
import br.com.knowledgeislands.model.enums.FilteredEnum;
import br.com.knowledgeislands.model.enums.OperationType;
import br.com.knowledgeislands.repository.GitRepositoryRepository;
import br.com.knowledgeislands.repository.GitRepositoryVersionRepository;
import br.com.knowledgeislands.repository.SharedLinkCommitRepository;
import br.com.knowledgeislands.utils.KnowledgeIslandsUtils;

@Service
public class FilterGitRepositoryService {

	@Autowired
	private GitRepositoryRepository projectRepository;
	@Autowired
	private GitRepositoryService projectService;
	@Autowired
	private FileService fileService;
	@Autowired
	private CommitService commitService;
	@Autowired
	private GitRepositoryVersionService gitRepositoryVersionService;
	@Autowired
	private GitRepositoryVersionRepository gitRepositoryVersionRepository;
	@Autowired
	private SharedLinkCommitRepository sharedLinkCommitRepository;

	public void filterEcoSpringHistory() throws URISyntaxException, IOException, InterruptedException {
		List<GitRepository> projects = projectRepository.findAll();
		filterProjectsByAge(projects);
		filterNotSoftwareProjects(projects);
		filterProjectsByInactive(projects);
		projectRepository.saveAll(projects);
	}

	public void filterEcoSpring() throws URISyntaxException, IOException, InterruptedException {
		List<GitRepository> projects = projectRepository.findAll();
		filterNotSoftwareProjects(projects);
		filterProjectsByInactive(projects);
		projectRepository.saveAll(projects);
	}

	public void filter(FilteringProjectsDTO form) throws URISyntaxException, IOException, InterruptedException {
		List<GitRepositoryVersion> versions = new ArrayList<GitRepositoryVersion>();
		java.io.File dir = new java.io.File(form.getFolderPath());
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				GitRepository project = projectService.returnProjectByPath(projectPath);
				GitRepositoryVersion version = gitRepositoryVersionService.getProjectVersionFiltering(projectPath);
				version.setGitRepository(project);
				versions.add(version);
			}
		}
		Map<String, List<GitRepositoryVersion>> versionMap = versions.stream().collect(Collectors.groupingBy(GitRepositoryVersion::getRepositoryLanguage));
		for(var entry: versionMap.entrySet()) {
			filterProjectBySize(entry.getValue());
		}
		List<GitRepository> projects = versions.stream().map(v -> v.getGitRepository()).toList();
		filterProjectsByAge(projects);
		for (GitRepository project : projects) {
			if(project.getLanguage() == null) {
				project.setFiltered(true);
				project.setFilteredReason(FilteredEnum.NOT_THE_ANALYZED_LANGUAGE);
				projectRepository.save(project);
			}
		}
		filterNotSoftwareProjects(projects);
		filterProjectsByInactive(projects);
		for(GitRepositoryVersion version: versions) {
			if(!version.getGitRepository().isFiltered() && filterProjectByCommits(version)) {
				version.getGitRepository().setFiltered(true);
				version.getGitRepository().setFilteredReason(FilteredEnum.HISTORY_MIGRATION);
				projectRepository.save(version.getGitRepository());
			}
		}
	}

	public void filterNotSoftwareProjects(List<GitRepository> projects) {
		List<String> notProjectSoftwareNames = KnowledgeIslandsUtils.projectsToRemoveInFiltering();
		for (GitRepository project : projects) {
			if(notProjectSoftwareNames.contains(project.getFullName()) && project.isFiltered() == false) {
				project.setFiltered(true);
				project.setFilteredReason(FilteredEnum.NOT_SOFTWARE_PROJECT);
				projectRepository.save(project);
			}
		}
	}

	public void filterProjectsByInactive(List<GitRepository> projects) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int calendarType = Calendar.YEAR;
		c.add(calendarType, -KnowledgeIslandsUtils.intervalYearsProjectConsideredInactivate);
		for (GitRepository project : projects) {
			if(project.getDownloadDate() != null && 
					project.getDownloadDate().before(c.getTime()) && project.isFiltered() == false) {
				project.setFiltered(true);
				project.setFilteredReason(FilteredEnum.INACTIVE_PROJECT);
				projectRepository.save(project);
			}
		}
	}

	public void filterProjectsByAge(List<GitRepository> projects) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int calendarType = Calendar.YEAR;
		c.add(calendarType, -KnowledgeIslandsUtils.intervalYearsProjectAgeFilter);
		for (GitRepository project : projects) {
			if(project.getFirstCommitDate() != null && 
					project.getFirstCommitDate().after(c.getTime()) && project.isFiltered() == false) {
				project.setFiltered(true);
				project.setFilteredReason(FilteredEnum.PROJECT_AGE);
				projectRepository.save(project);
			}
		}
	}

	private boolean filterProjectByCommits(GitRepositoryVersion version) throws IOException {
		List<File> allFiles = version.getFiles();
		List<Commit> commits = version.getCommits();
		Collections.sort(commits);
		commits = commits.subList(0, 20);
		int numberOfFiles = allFiles.size();
		List<File> addedFiles = new ArrayList<>();
		for(Commit commit: commits) {
			for (CommitFile commitFile : commit.getCommitFiles()) {
				if(commitFile.getStatus().equals(OperationType.ADDED)) {
					addedFiles.add(commitFile.getFile());
				}
			}
		}
		int numberOfCurrentFilesAdded = 0;
		for (File file : addedFiles) {
			if(allFiles.stream().anyMatch(f -> f.isFile(file.getPath()))) {
				numberOfCurrentFilesAdded++;
			}
		}
		return numberOfCurrentFilesAdded >= (numberOfFiles*0.5);
	}

	private List<GitRepository> filterProjectBySize(List<GitRepositoryVersion> versions) {
		List<Double> devs = versions.stream().map(v -> Double.valueOf(v.getNumberAnalysedDevs())).toList();
		List<Double> commits = versions.stream().map(v -> Double.valueOf(v.getNumberAnalysedCommits())).toList();
		List<Double> files = versions.stream().map(v -> Double.valueOf(v.getNumberAnalysedFiles())).toList();
		double[] devsArray = new double[devs.size()];
		for (int i = 0; i < devs.size(); i++) {
			devsArray[i] = devs.get(i);
		}
		double[] commitsArray = new double[commits.size()];
		for (int i = 0; i < commits.size(); i++) {
			commitsArray[i] = commits.get(i);
		}
		double[] filesArray = new double[files.size()];
		for (int i = 0; i < files.size(); i++) {
			filesArray[i] = files.get(i);
		}
		Percentile p = new Percentile();
		double firstQDevs = p.evaluate(devsArray, 75);
		double firstQCommits = p.evaluate(commitsArray, 50);
		double firstQFiles = p.evaluate(filesArray, 50);
		Set<GitRepository> projects = new HashSet<>();
		projects.addAll(versions.stream().filter(v -> v.getNumberAnalysedDevs() < firstQDevs).map(v -> v.getGitRepository()).toList());
		projects.addAll(versions.stream().filter(v -> v.getNumberAnalysedCommits() < firstQCommits).map(v -> v.getGitRepository()).toList());
		projects.addAll(versions.stream().filter(v -> v.getNumberAnalysedFiles() < firstQFiles).map(v -> v.getGitRepository()).toList());
		projects.stream().forEach(pr -> pr.setFilteredReason(FilteredEnum.SIZE));
		projects.stream().forEach(pr -> pr.setFiltered(true));
		for (GitRepository project : projects) {
			projectRepository.save(project);
		}
		return new ArrayList<>(projects);
	}

	//	public void filteringSharedLinkProjects() {
	//		List<GitRepository> repositories = sharedLinkCommitRepository.findRepositoriesBySharedLinkCommitWithCommitFile();
	//		List<GitRepositoryVersion> versions = repositories.stream().map(r -> r.getGitRepositoryVersion().get(0)).toList();
	//		filterProjectBySize(versions);
	//	}

	public void filteringSharedLinkProjects() throws IOException {
		List<GitRepository> repositories = sharedLinkCommitRepository.findRepositoriesBySharedLinkCommitWithCommitFile();
		List<GitRepositoryVersion> versions = repositories.stream().map(r -> r.getGitRepositoryVersion().get(0)).toList();
		List<GitRepository> repositoriesFilteredBySize = filterProjectBySize2(versions);
		repositories.removeAll(repositoriesFilteredBySize);
		versions = repositories.stream().map(r -> r.getGitRepositoryVersion().get(0)).toList();
		for(GitRepositoryVersion version: versions) {
			if(filterProjectByCommits(version)) {
				version.getGitRepository().setFiltered(true);
				version.getGitRepository().setFilteredReason(FilteredEnum.HISTORY_MIGRATION);
				projectRepository.save(version.getGitRepository());
			}
		}
	}

	private List<GitRepository> filterProjectBySize2(List<GitRepositoryVersion> versions) {
		Set<GitRepository> projects = new HashSet<>();
		projects.addAll(versions.stream().filter(v -> v.getNumberAnalysedDevs() < 39).map(v -> v.getGitRepository()).toList());
		projects.stream().forEach(pr -> pr.setFilteredReason(FilteredEnum.SIZE));
		projects.stream().forEach(pr -> pr.setFiltered(true));
		for (GitRepository project : projects) {
			projectRepository.save(project);
		}
		return new ArrayList<>(projects);
	}

	@Transactional
	public void filteringProjectsNotSoftware(List<String> fullNames) {
		List<GitRepository> repositories = projectRepository.findByFullNameIn(fullNames);
		repositories.stream().forEach(r -> {
			r.setFiltered(true);
			r.setFilteredReason(FilteredEnum.NOT_SOFTWARE_PROJECT);
		});
		projectRepository.saveAll(repositories);
	}

	@Transactional
	public void filteringProjectsSize(List<String> fullNames) {
		List<GitRepository> repositories = projectRepository.findByFullNameIn(fullNames);
		repositories.stream().forEach(r -> {
			r.setFiltered(true);
			r.setFilteredReason(FilteredEnum.SIZE);
		});
		projectRepository.saveAll(repositories);
	}

}
