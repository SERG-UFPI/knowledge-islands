package br.com.gitanalyzer.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gitanalyzer.extractors.CommitExtractor;
import br.com.gitanalyzer.extractors.FileExtractor;
import br.com.gitanalyzer.extractors.GitRepositoryFolderExtractor;
import br.com.gitanalyzer.extractors.GitRepositoryTruckFactorExtractor;
import br.com.gitanalyzer.model.Commit;
import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.ContributorVersion;
import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.FileVersion;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.entity.GitRepositoryFolder;
import br.com.gitanalyzer.model.entity.GitRepositoryVersion;
import br.com.gitanalyzer.model.entity.GitRepositoryVersionKnowledgeModel;
import br.com.gitanalyzer.repository.GitRepositoryRepository;
import br.com.gitanalyzer.repository.GitRepositoryVersionKnowledgeModelRepository;
import br.com.gitanalyzer.repository.GitRepositoryVersionRepository;
import br.com.gitanalyzer.utils.AsyncUtils;
import br.com.gitanalyzer.utils.ContributorUtils;

@Service
public class GitRepositoryVersionService {

	private FileExtractor fileExtractor = new FileExtractor();
	private CommitExtractor commitExtractor = new CommitExtractor();
	private ContributorUtils contributorUtils = new ContributorUtils();
	private GitRepositoryTruckFactorExtractor projectVersionExtractor = new GitRepositoryTruckFactorExtractor();
	private GitRepositoryFolderExtractor gitRepositoryFolderExtractor = new GitRepositoryFolderExtractor();

	@Autowired
	private GitRepositoryVersionRepository gitRepositoryVersionRepository;
	@Autowired
	private GitRepositoryDependencyService projectDependencyService;
	@Autowired
	private GitRepositoryRepository gitRepositoryRepository;
	@Autowired
	private GitRepositoryService gitRepositoryService;
	@Autowired
	private GitRepositoryVersionKnowledgeModelRepository gitRepositoryVersionKnowledgeModelRepository;

	public void remove(Long id) {
		gitRepositoryVersionRepository.deleteById(id);
	}

	public void removeAll() {
		List<GitRepositoryVersion> versions = gitRepositoryVersionRepository.findAll();
		List<Long> ids = versions.stream().map(v -> v.getId()).toList();

		ExecutorService executorService = AsyncUtils.getExecutorServiceForLogs();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (Long id : ids) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
				gitRepositoryVersionRepository.deleteById(id);
			}, executorService);
			futures.add(future);
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		executorService.shutdown();
	}

	@Transactional
	public void removeFromProject(Long id) {
		gitRepositoryVersionRepository.deleteByGitRepositoryId(id);
	}

	@Transactional
	public void removeFromProjectsFiltered() {
		List<GitRepository> projects = gitRepositoryRepository.findByFilteredTrue();
		List<Long> ids = projects.stream().map(p -> p.getId()).toList();
		gitRepositoryVersionRepository.deleteByGitRepositoryIdIn(ids);
	}

	public GitRepositoryVersion extractProjectVersion(GitRepository repository) throws IOException {
		long start = System.currentTimeMillis();
		if(repository.getCurrentFolderPath().substring(repository.getCurrentFolderPath().length() -1).equals("/") == false) {
			repository.setCurrentFolderPath(repository.getCurrentFolderPath()+"/");
		}
		List<File> files = fileExtractor.extractFilesFromClocFile(repository.getCurrentFolderPath(), repository.getName());
		fileExtractor.getRenamesFiles(repository.getCurrentFolderPath(), files);
		List<Commit> commits = commitExtractor.extractCommitsFromLogFiles(repository.getCurrentFolderPath());
		Collections.sort(commits, Collections.reverseOrder());
		Date dateVersion = commits.get(0).getAuthorDate();
		String versionId = commits.get(0).getSha();
		commitExtractor.extractCommitsFiles(repository.getCurrentFolderPath(), commits, files);
		commits.removeIf(c -> c.getCommitFiles().size() == 0);
		commitExtractor.extractCommitsFileAndDiffsOfCommits(repository.getCurrentFolderPath(), commits, files);
		List<Contributor> contributors = projectVersionExtractor.extractContributorFromCommits(commits);
		contributors = projectVersionExtractor.setAlias(contributors, repository.getName());
		contributors = contributors.stream().filter(c -> c.getEmail() != null && c.getName() != null).toList();
		long end = System.currentTimeMillis();
		List<String> filesPaths = files.stream().map(f -> repository.getCurrentFolderPath()+f.getPath()).toList();
		GitRepositoryFolder gitRepositoryFolder = gitRepositoryFolderExtractor.getGitRepositoryFolder(repository.getCurrentFolderPath(), repository.getCurrentFolderPath(), filesPaths);
		float sec = (end - start) / 1000F;
		GitRepositoryVersion projectVersion = new GitRepositoryVersion(contributors.size(), 
				files.size(), commits.size(), 
				dateVersion, versionId, contributorUtils.setActiveContributors(contributors, commits),
				commits, files, (double) sec, projectDependencyService.getDependenciesProjectVersion(repository.getFullName()), gitRepositoryFolder);
		return projectVersion;
	}

	@Transactional
	public GitRepositoryVersion saveGitRepositoryVersion(String repositoryPath) throws Exception {
		//gitRepositoryService.generateLogFiles(repositoryPath);
		GitRepository gitRepository = gitRepositoryService.saveGitRepository(repositoryPath);
		System.out.println("BEGIN SAVING GIT REPOSITORY VERSION: "+repositoryPath);
		GitRepositoryVersion gitRepositoryVersion = extractProjectVersion(gitRepository);
		if(!gitRepositoryVersion.validTruckFactor()) {
			throw new Exception("GitRepository version not valid");
		}
		if(!gitRepositoryVersionRepository.existsByVersionIdAndGitRepositoryId(gitRepositoryVersion.getVersionId(), gitRepository.getId())) {
			gitRepositoryVersion.setGitRepository(gitRepository);
			gitRepositoryVersionRepository.save(gitRepositoryVersion);
		}else {
			throw new Exception("GitRepository version already extracted");
		}
		System.out.println("ENDING SAVING GIT REPOSITORY VERSION: "+repositoryPath);
		return gitRepositoryVersion;
	}

	public GitRepositoryVersion getGitRepositoryVersionById(Long id) {
		GitRepositoryVersion gitRepositoryVersion = gitRepositoryVersionRepository.findById(id).get();
		List<GitRepositoryVersionKnowledgeModel> models = gitRepositoryVersionKnowledgeModelRepository.findByRepositoryVersionId(gitRepositoryVersion.getId());
		setTruckFactorsFolders(gitRepositoryVersion.getRootFolder(), models);
		return gitRepositoryVersion;
	}

	private void setTruckFactorsFolders(GitRepositoryFolder rootFolder,
			List<GitRepositoryVersionKnowledgeModel> models) {
		for (GitRepositoryVersionKnowledgeModel gitRepositoryVersionKnowledgeModel : models) {
			if((gitRepositoryVersionKnowledgeModel.getFoldersPaths() == null || gitRepositoryVersionKnowledgeModel.getFoldersPaths().isEmpty())
					&& rootFolder.getPath() == null) {
				setRootFolderTruckFactorInfo(gitRepositoryVersionKnowledgeModel, rootFolder);
			}
			if(gitRepositoryVersionKnowledgeModel.getFoldersPaths() != null && 
					gitRepositoryVersionKnowledgeModel.getFoldersPaths().size() == 1) {
				String folderPath = gitRepositoryVersionKnowledgeModel.getFoldersPaths().get(0);
				if(folderPath.equals(rootFolder.getPath())) {
					setRootFolderTruckFactorInfo(gitRepositoryVersionKnowledgeModel, rootFolder);
				}
			}
		}
		if(rootFolder.getChildren() != null && !rootFolder.getChildren().isEmpty()) {
			for (GitRepositoryFolder folder: rootFolder.getChildren()) {
				setTruckFactorsFolders(folder, models);
			}
		}
	}

	private void setRootFolderTruckFactorInfo(GitRepositoryVersionKnowledgeModel gitRepositoryVersionKnowledgeModel, GitRepositoryFolder rootFolder) {
		rootFolder.setTruckFactor(gitRepositoryVersionKnowledgeModel.getTruckFactor());
		Collections.sort(gitRepositoryVersionKnowledgeModel.getFiles());
		rootFolder.setFiles(gitRepositoryVersionKnowledgeModel.getFiles().stream().limit(20).toList());
		for (FileVersion fileVersion : rootFolder.getFiles()) {
			forContributor: for (ContributorVersion contributor : gitRepositoryVersionKnowledgeModel.getContributors()) {
				if(contributor.getContributor().isActive()) {
					for (File file: contributor.getFilesAuthor()) {
						if(file.isFile(fileVersion.getFile().getPath())) {
							fileVersion.setNumberActiveAuthor(fileVersion.getNumberActiveAuthor()+1);
							continue forContributor;
						}
					}
				}
			}
		}
		for (ContributorVersion contributorVersion : rootFolder.getTruckFactor().getContributors()) {
			if(contributorVersion.getFilesAuthor() != null && contributorVersion.getFilesAuthor().size() > 0) {
				contributorVersion.setFilesAuthorPath(contributorVersion.getFilesAuthor().stream().map(f -> f.getPath()).toList());
			}
		}
	}

}