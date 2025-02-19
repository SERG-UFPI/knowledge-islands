package br.com.knowledgeislands.service;

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

import br.com.knowledgeislands.exceptions.NoCommitForRepositoryException;
import br.com.knowledgeislands.extractors.GitRepositoryFolderExtractor;
import br.com.knowledgeislands.model.entity.Commit;
import br.com.knowledgeislands.model.entity.Contributor;
import br.com.knowledgeislands.model.entity.ContributorVersion;
import br.com.knowledgeislands.model.entity.File;
import br.com.knowledgeislands.model.entity.FileVersion;
import br.com.knowledgeislands.model.entity.GitRepository;
import br.com.knowledgeislands.model.entity.GitRepositoryFolder;
import br.com.knowledgeislands.model.entity.GitRepositoryVersion;
import br.com.knowledgeislands.model.entity.GitRepositoryVersionKnowledgeModel;
import br.com.knowledgeislands.repository.FileRepositorySharedLinkCommitRepository;
import br.com.knowledgeislands.repository.GitRepositoryRepository;
import br.com.knowledgeislands.repository.GitRepositoryVersionKnowledgeModelRepository;
import br.com.knowledgeislands.repository.GitRepositoryVersionRepository;
import br.com.knowledgeislands.repository.SharedLinkRepository;
import br.com.knowledgeislands.utils.KnowledgeIslandsUtils;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class GitRepositoryVersionService {

	private GitRepositoryFolderExtractor gitRepositoryFolderExtractor = new GitRepositoryFolderExtractor();

	@Autowired
	private GitRepositoryVersionRepository gitRepositoryVersionRepository;
	@Autowired
	private GitRepositoryRepository gitRepositoryRepository;
	@Autowired
	private GitRepositoryService gitRepositoryService;
	@Autowired
	private GitRepositoryVersionKnowledgeModelRepository gitRepositoryVersionKnowledgeModelRepository;
	@Autowired
	private SharedLinkRepository sharedLinkRepository;
	@Autowired
	private FileService fileService;
	@Autowired
	private CommitService commitService;
	@Autowired
	private ContributorService contributorService;
	@Autowired
	private FileRepositorySharedLinkCommitRepository fileGitRepositorySharedLinkCommitRepository;
	@Autowired
	private SharedLinkCommitService sharedLinkCommitService;

	public void remove(Long id) {
		gitRepositoryVersionRepository.deleteById(id);
	}

	public void removeAll() {
		sharedLinkRepository.deleteAll();
		List<GitRepositoryVersion> versions = gitRepositoryVersionRepository.findAll();
		List<Long> ids = versions.stream().map(v -> v.getId()).toList();

		ExecutorService executorService = KnowledgeIslandsUtils.getExecutorServiceMax();
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

	public GitRepositoryVersion getProjectVersion(GitRepository repository) throws IOException, NoCommitForRepositoryException {
		long start = System.currentTimeMillis();
		repository.setCurrentFolderPath(KnowledgeIslandsUtils.fixFolderPath(repository.getCurrentFolderPath()));
		log.info("Reading files...");
		List<File> files = fileService.getFilesFromClocFile(repository);
		fileService.getRenamesFiles(repository.getCurrentFolderPath(), files);
		log.info("Reading commits...");
		List<Commit> commits = commitService.getCommitsFromLogFiles(repository);
		if(commits != null && !commits.isEmpty()) {
			Collections.sort(commits, Collections.reverseOrder());
			Commit latestCommit = commits.get(0);
			Date dateVersion = latestCommit.getAuthorDate();
			String versionId = latestCommit.getSha();
			log.info("Reading commit files...");
			commitService.getCommitsFiles(repository, commits, files);
			log.info("Reading diffs...");
			commitService.getCommitsFileAndDiffsOfCommits(repository.getCurrentFolderPath(), commits);
			List<Contributor> contributors = contributorService.getContributorFromCommits(commits);
			log.info("Setting aliases...");
			contributors = contributors.stream().filter(c -> c.getEmail() != null && c.getName() != null).toList();
			contributors = contributorService.setAlias(contributors);
			List<String> filesPaths = files.stream().map(f -> repository.getCurrentFolderPath()+f.getPath()).toList();
			GitRepositoryFolder gitRepositoryFolder = gitRepositoryFolderExtractor.getGitRepositoryFolder(repository.getCurrentFolderPath(), repository.getCurrentFolderPath(), filesPaths);
			float sec = (System.currentTimeMillis() - start) / 1000F;
			return new GitRepositoryVersion(repository, contributors.size(), 
					files.size(), commits.size(), 
					dateVersion, versionId, contributorService.setActiveContributors(contributors, commits, dateVersion),
					commits, files, (double) sec, gitRepositoryFolder);
		}else {
			throw new NoCommitForRepositoryException(repository.getFullName());
		}
	}

	@Transactional
	public GitRepositoryVersion saveGitRepositoryAndGitRepositoryVersion(String repositoryPath) throws Exception {
		return saveGitRepositoryVersion(gitRepositoryService.saveGitRepository(repositoryPath));
	}

	public GitRepositoryVersion saveGitRepositoryVersion(GitRepository gitRepository) throws Exception {
		log.info("====== BEGIN SAVING GIT REPOSITORY VERSION: "+gitRepository.getFullName());
		GitRepositoryVersion gitRepositoryVersion = getProjectVersion(gitRepository);
		if(gitRepositoryVersion.validGitRepositoryVersion()) {
			gitRepositoryVersionRepository.save(gitRepositoryVersion);
		}else {
			throw new Exception("GitRepository version not valid");
		}
		log.info("====== ENDING SAVING GIT REPOSITORY VERSION: "+gitRepository.getFullName());
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
		setNumberActiveAuthorsFiles(gitRepositoryVersionKnowledgeModel);
		Collections.sort(gitRepositoryVersionKnowledgeModel.getFiles());
		rootFolder.setFiles(gitRepositoryVersionKnowledgeModel.getFiles().stream().limit(20).toList());
		for (ContributorVersion contributorVersion : rootFolder.getTruckFactor().getContributors()) {
			if(contributorVersion.getFilesAuthor() != null && !contributorVersion.getFilesAuthor().isEmpty()) {
				contributorVersion.setFilesAuthorPath(contributorVersion.getFilesAuthor().stream().map(f -> f.getPath()).toList());
			}
		}
	}

	private void setNumberActiveAuthorsFiles(GitRepositoryVersionKnowledgeModel gitRepositoryVersionKnowledgeModel) {
		for (FileVersion fileVersion : gitRepositoryVersionKnowledgeModel.getFiles()) {
			forContributor: for (ContributorVersion contributor : gitRepositoryVersionKnowledgeModel.getTruckFactor().getContributors()) {
				//if(contributor.getContributor().isActive()) {
				for (File file: contributor.getFilesAuthor()) {
					if(file.isFile(fileVersion.getFile().getPath())) {
						fileVersion.setNumberActiveAuthor(fileVersion.getNumberActiveAuthor()+1);
						continue forContributor;
					}
				}
				//}
			}
		}
	}

	public GitRepositoryVersion getProjectVersionFiltering(String projectPath) throws IOException {
		List<Commit> commits = null;//commitService.getCommitsFromLogFiles(projectPath);
		int numberAllCommits = commits.size();
		List<Contributor> contributors = contributorService.getContributorFromCommits(commits);
		contributors = contributorService.setAlias(contributors);
		int numberAnalysedDevs = contributors.size();
		return GitRepositoryVersion.builder().numberAnalysedCommits(numberAllCommits).numberAnalysedDevs(numberAnalysedDevs).build();
	}

	public void saveGitRepositoriesVersionSharedLinkGenAi() {
		List<GitRepositoryVersion> grvs = saveGitRepositoriesVersionSharedLink();
		for (GitRepositoryVersion gitRepositoryVersion: grvs) {
			try {
				sharedLinkCommitService.setCommitCopiedLineOfRepository(gitRepositoryVersion.getId());
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e.getMessage());
			}
		}
	}

	public List<GitRepositoryVersion> saveGitRepositoriesVersionSharedLink() {
		List<GitRepositoryVersion> grvs = new ArrayList<>();
		List<GitRepository> repositories = fileGitRepositorySharedLinkCommitRepository.findDistinctGitRepositoriesWithNonNullConversationAndCurrentFolderPathIsNotNull();
		for (GitRepository gitRepository: repositories) {
			try {
				grvs.add(saveGitRepositoryVersion(gitRepository));
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e.getMessage());
			}
		}
		return grvs;
	}

	public void saveGitRepositoryVersionGenai(String repositoryPath) {
		GitRepository gitRepository = gitRepositoryRepository.findByCurrentFolderPath(repositoryPath);
		try {
			saveGitRepositoryVersion(gitRepository);
			sharedLinkCommitService.setCommitCopiedLineOfRepository(saveGitRepositoryVersion(gitRepository).getId());
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}

	public void saveSharedLinkCommitsVersions() {
		List<GitRepositoryVersion> grvs = gitRepositoryVersionRepository.findAll();
		for (GitRepositoryVersion gitRepositoryVersion: grvs) {
			try {
				sharedLinkCommitService.setCommitCopiedLineOfRepository(gitRepositoryVersion.getId());
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e.getMessage());
			}
		}
	}

	public void saveGitRepositoryVersionNotFiltered() throws Exception {
		List<GitRepository> repositories = gitRepositoryRepository.findByFilteredFalse();
		for (GitRepository repository : repositories) {
			saveGitRepositoryVersion(repository);
		}
	}

	public void saveByGitRepositoryId(Long idGitRepository) throws Exception {
		GitRepository repository = gitRepositoryRepository.findById(idGitRepository).get();
		saveGitRepositoryVersion(repository);
	}

}