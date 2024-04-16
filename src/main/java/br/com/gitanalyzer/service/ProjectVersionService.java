package br.com.gitanalyzer.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gitanalyzer.extractors.CommitExtractor;
import br.com.gitanalyzer.extractors.FileExtractor;
import br.com.gitanalyzer.extractors.GitRepositoryVersionExtractor;
import br.com.gitanalyzer.model.Commit;
import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.entity.GitRepositoryVersion;
import br.com.gitanalyzer.repository.GitRepositoryRepository;
import br.com.gitanalyzer.repository.RepositoryVersionRepository;
import br.com.gitanalyzer.utils.AsyncUtils;
import br.com.gitanalyzer.utils.CommitUtils;
import br.com.gitanalyzer.utils.ContributorUtils;

@Service
public class ProjectVersionService {
	
	private FileExtractor fileExtractor = new FileExtractor();
	private CommitExtractor commitExtractor = new CommitExtractor();
	private ContributorUtils contributorUtils = new ContributorUtils();
	//private CkMeasuresExtractor ckMeasuresExtractor = new CkMeasuresExtractor();
	private GitRepositoryVersionExtractor projectVersionExtractor = new GitRepositoryVersionExtractor();

	@Autowired
	private RepositoryVersionRepository repository;
	@Autowired
	private ProjectDependencyService projectDependencyService;
	@Autowired
	private GitRepositoryRepository projectRepository;

	public void remove(Long id) {
		repository.deleteById(id);
	}

	public void removeAll() {
		List<GitRepositoryVersion> versions = repository.findAll();
		List<Long> ids = versions.stream().map(v -> v.getId()).toList();

		ExecutorService executorService = AsyncUtils.getExecutorServiceForLogs();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (Long id : ids) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
				repository.deleteById(id);
			}, executorService);
			futures.add(future);
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		executorService.shutdown();
	}

	@Transactional
	public void removeFromProject(Long id) {
		repository.deleteByRepositoryId(id);
	}

	@Transactional
	public void removeFromProjectsFiltered() {
		List<GitRepository> projects = projectRepository.findByFilteredTrue();
		List<Long> ids = projects.stream().map(p -> p.getId()).toList();
		repository.deleteByRepositoryIdIn(ids);
	}
	
	public GitRepositoryVersion extractProjectVersion(GitRepository project) throws IOException {
		long start = System.currentTimeMillis();
		System.out.println("EXTRACTING PROJECT VERSION OF "+project.getName());
		if(project.getCurrentPath().substring(project.getCurrentPath().length() -1).equals("/") == false) {
			project.setCurrentPath(project.getCurrentPath()+"/");
		}
//		QualityMeasures qualityMeasures = null;
//		if(project.getMainLanguage() != null && project.getMainLanguage().equals("Java")) {
//			qualityMeasures = ckMeasuresExtractor.extract(project.getCurrentPath());
//		}
		int numberAllFiles = fileExtractor.extractSizeAllFiles(project.getCurrentPath());
		List<File> files = fileExtractor.extractFilesFromClocFile(project.getCurrentPath(), project.getName());
		int numberAnalysedFiles = files.size();
		fileExtractor.getRenamesFiles(project.getCurrentPath(), files);
		List<Commit> commits = commitExtractor.extractCommitsFromLogFiles(project.getCurrentPath());
		Date dateVersion = commits.get(0).getDate();
		String versionId = commits.get(0).getExternalId();
		int numberAllCommits = commits.size();
		commits = commitExtractor.extractCommitsFiles(project.getCurrentPath(), commits, files);
		commits.removeIf(c -> c.getCommitFiles().size() == 0);
		commits = commitExtractor.extractCommitsFileAndDiffsOfCommits(project.getCurrentPath(), commits, files);
		int numberAnalysedCommits = commits.size();
		List<Contributor> contributors = projectVersionExtractor.extractContributorFromCommits(commits);
		contributors = projectVersionExtractor.setAlias(contributors, project.getName());
		contributors = contributors.stream().filter(c -> c.getEmail() != null && c.getName() != null).toList();
		int numberAnalysedDevs = contributors.size();
		CommitUtils.sortCommitsByDate(commits);
		long end = System.currentTimeMillis();
		float sec = (end - start) / 1000F;
		GitRepositoryVersion projectVersion = new GitRepositoryVersion(numberAnalysedDevs, 
				numberAllFiles, numberAnalysedFiles, numberAllCommits, numberAnalysedCommits, 
				dateVersion, versionId, contributorUtils.setActiveContributors(contributors, commits),
				commits, files, (double) sec, projectDependencyService.getDependenciesProjectVersion(project.getFullName()));
		return projectVersion;
	}

}