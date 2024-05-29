package br.com.gitanalyzer.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gitanalyzer.dto.form.CloneRepoForm;
import br.com.gitanalyzer.dto.form.GitRepositoryVersionKnowledgeModelForm1;
import br.com.gitanalyzer.dto.form.HistoryReposTruckFactorForm;
import br.com.gitanalyzer.dto.form.RepositoryKnowledgeMetricForm;
import br.com.gitanalyzer.enums.GitRepositoryVersionProcessStageEnum;
import br.com.gitanalyzer.enums.KnowledgeModel;
import br.com.gitanalyzer.extractors.HistoryCommitsExtractor;
import br.com.gitanalyzer.model.entity.ContributorVersion;
import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.FileVersion;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.entity.GitRepositoryFolder;
import br.com.gitanalyzer.model.entity.GitRepositoryVersion;
import br.com.gitanalyzer.model.entity.GitRepositoryVersionKnowledgeModel;
import br.com.gitanalyzer.model.entity.GitRepositoryVersionProcess;
import br.com.gitanalyzer.model.entity.TruckFactor;
import br.com.gitanalyzer.repository.GitRepositoryVersionKnowledgeModelRepository;
import br.com.gitanalyzer.repository.GitRepositoryVersionProcessRepository;
import br.com.gitanalyzer.repository.GitRepositoryVersionRepository;
import br.com.gitanalyzer.repository.TruckFactorRepository;
import br.com.gitanalyzer.utils.AsyncUtils;

@Service
public class TruckFactorService {

	@Autowired
	private GitRepositoryVersionService gitRepositoryVersionService;
	@Autowired
	private GitRepositoryVersionKnowledgeModelRepository gitRepositoryVersionKnowledgeModelRepository;
	@Autowired
	private GitRepositoryVersionKnowledgeModelService gitRepositoryVersionKnowledgeModelService;
	@Autowired
	private TruckFactorRepository truckFactorRepository;
	@Autowired
	private GitRepositoryService gitRepositoryService;
	@Autowired
	private DownloaderService downloaderService;
	@Autowired
	private GitRepositoryVersionProcessRepository gitRepositoryVersionProcessRepository;
	@Autowired
	private GitRepositoryVersionRepository gitRepositoryVersionRepository;

	@Async
	@Transactional
	public void continueProcesses(GitRepositoryVersionProcess process, CloneRepoForm form) {
		try {
			setProcessStage(process, GitRepositoryVersionProcessStageEnum.DOWNLOADING);
			String projectPath = downloaderService.cloneProject(form);
			setProcessStage(process, GitRepositoryVersionProcessStageEnum.EXTRACTING_DATA);
			gitRepositoryService.generateLogFiles(projectPath);
			GitRepositoryVersion gitRepositoryVersion = gitRepositoryVersionService.saveGitRepositoryVersion(projectPath);
			saveGitRepositoryVersionKnowledgeModelTruckFactorAllFolders(gitRepositoryVersion, KnowledgeModel.DOE);
			process.setGitRepositoryVersion(gitRepositoryVersion);
			process.setEndDate(new Date());
			setProcessStage(process, GitRepositoryVersionProcessStageEnum.EXTRACTION_FINISHED);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional
	public void setProcessStage(GitRepositoryVersionProcess process, GitRepositoryVersionProcessStageEnum stage) {
		process.setStage(stage);
		gitRepositoryVersionProcessRepository.save(process);
	}

	public TruckFactor generateLogsTruckFactorRepository(RepositoryKnowledgeMetricForm form) throws Exception {
		gitRepositoryService.generateLogFiles(form.getRepositoryPath());
		return generateTruckFactorRepository(form);
	}

	@Transactional
	public TruckFactor saveTruckFactor(Long idGitRepositoryVersionKnowledgeModel) {
		long start = System.currentTimeMillis();
		GitRepositoryVersionKnowledgeModel gitRepositoryVersionKnowledgeModel = gitRepositoryVersionKnowledgeModelRepository.findById(idGitRepositoryVersionKnowledgeModel).get();
		gitRepositoryVersionKnowledgeModel.getContributors().removeIf(ckm -> ckm.getNumberFilesAuthor() == 0);
		Collections.sort(gitRepositoryVersionKnowledgeModel.getContributors(), Collections.reverseOrder());
		List<ContributorVersion> topContributors = new ArrayList<>();
		double fileSize = gitRepositoryVersionKnowledgeModel.getFiles().size();
		int tf = 0;
		while(gitRepositoryVersionKnowledgeModel.getContributors().isEmpty() == false) {
			double numberFilesCovarage = getCoverageFiles(gitRepositoryVersionKnowledgeModel.getContributors(), gitRepositoryVersionKnowledgeModel.getFiles()).size();
			double coverage = numberFilesCovarage/fileSize;
			if(coverage < 0.5) 
				break;
			topContributors.add(gitRepositoryVersionKnowledgeModel.getContributors().get(0));
			gitRepositoryVersionKnowledgeModel.getContributors().remove(0);
			tf = tf+1;
		}
		List<FileVersion> coveredFiles = getCoverageFiles(topContributors, gitRepositoryVersionKnowledgeModel.getFiles());
		long end = System.currentTimeMillis();
		float sec = (end - start) / 1000F;
		TruckFactor truckFactor = new TruckFactor(tf, gitRepositoryVersionKnowledgeModel, coveredFiles, topContributors, (double) sec);
		truckFactorRepository.save(truckFactor);
		gitRepositoryVersionKnowledgeModel.setTruckFactor(truckFactor);
		gitRepositoryVersionKnowledgeModelRepository.save(gitRepositoryVersionKnowledgeModel);
		return truckFactor;
	}

	@Transactional
	public TruckFactor generateTruckFactorRepository(RepositoryKnowledgeMetricForm repo)
			throws Exception {
		GitRepositoryVersion gitRepositoryVersion = gitRepositoryVersionService.saveGitRepositoryVersion(repo.getRepositoryPath());
		GitRepositoryVersionKnowledgeModel gitRepositoryVersionKnowledgeModel = gitRepositoryVersionKnowledgeModelService.
				saveGitRepositoryVersionKnowledgeModel(new GitRepositoryVersionKnowledgeModelForm1(gitRepositoryVersion.getId(), repo.getKnowledgeMetric(), repo.getFoldersPaths()));
		TruckFactor truckFactor = saveTruckFactor(gitRepositoryVersionKnowledgeModel.getId());
		return truckFactor;
	}

	public void directoriesTruckFactorAnalyzes(RepositoryKnowledgeMetricForm request) throws IOException, NoHeadException, GitAPIException{
		ExecutorService executorService = AsyncUtils.getExecutorServiceForTf();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		java.io.File dir = new java.io.File(request.getRepositoryPath());
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				GitRepository project = gitRepositoryService.returnProjectByPath(projectPath);
				if(project != null && project.isFiltered() == false) {
					CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
						try {
							RepositoryKnowledgeMetricForm repo = new RepositoryKnowledgeMetricForm(projectPath, request.getKnowledgeMetric());
							generateTruckFactorRepository(repo);
						} catch (Exception e) {
							e.printStackTrace();
						}finally {
							executorService.shutdown();
						}
					}, executorService);
					futures.add(future);
				}
			}
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		executorService.shutdown();
	}

	protected List<FileVersion> getCoverageFiles(List<ContributorVersion> contributorsVersion, List<FileVersion> filesVersion) {
		List<FileVersion> files = new ArrayList<>();
		forFiles:for(FileVersion file: filesVersion) {
			for (ContributorVersion contributor : contributorsVersion) {
				if(contributor.getNumberFilesAuthor() > 0) {
					for(File fileContributor: contributor.getFilesAuthor()) {
						if (file.getFile().isFile(fileContributor.getPath())) {
							files.add(file);
							continue forFiles;
						}
					}
				}
			}
		}
		return files;
	}

	public void historyReposTruckFactor(HistoryReposTruckFactorForm form) throws URISyntaxException, IOException, InterruptedException, NoHeadException, GitAPIException {
		ExecutorService executorService = AsyncUtils.getExecutorServiceForTf();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		java.io.File dir = new java.io.File(form.getPath());
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				GitRepository project = gitRepositoryService.returnProjectByPath(projectPath);
				if((project != null && project.isFiltered() == false) || project == null) {
					CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
						try {
							historyRepoTruckFactor(HistoryReposTruckFactorForm.builder()
									.knowledgeMetric(KnowledgeModel.DOE)
									.interval(form.getInterval())
									.intervalType( form.getIntervalType())
									.path(projectPath).build());
						} catch (IOException | GitAPIException | InterruptedException | URISyntaxException e) {
							e.printStackTrace();
						}finally {
							executorService.shutdown();
						}
					}, executorService);
					futures.add(future);
				}
			}
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		executorService.shutdown();
	}

	public void historyRepoTruckFactor(HistoryReposTruckFactorForm form) throws NoHeadException, IOException, GitAPIException, InterruptedException, URISyntaxException {
		HistoryCommitsExtractor historyCommitsExtractor = new HistoryCommitsExtractor();
		List<String> hashes = historyCommitsExtractor.getCommitHashesByInterval(form);
		try {
			for (String hash : hashes) {
				gitRepositoryService.checkOutProjectVersion(form.getPath(), hash);
				gitRepositoryService.generateLogFiles(form.getPath());
				generateTruckFactorRepository(RepositoryKnowledgeMetricForm.builder()
						.knowledgeMetric(form.getKnowledgeMetric()).repositoryPath(form.getPath()).build());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			gitRepositoryService.checkOutProjectVersion(form.getPath(), hashes.get(hashes.size()-1));
		}
	}

	public TruckFactor getTruckFactorById(Long id) throws Exception {
		TruckFactor truckFactor = truckFactorRepository.findById(id).orElse(null);
		if(truckFactor == null) {
			throw new Exception("Truck Factor not found with id "+id);
		}
		return truckFactor;
	}

	@Transactional
	public GitRepositoryVersionKnowledgeModel saveGitRepositoryVersionKnowledgeTruckFactor(GitRepositoryVersionKnowledgeModelForm1 form) throws Exception {
		GitRepositoryVersionKnowledgeModel gitRepositoryVersionKnowledgeModel = gitRepositoryVersionKnowledgeModelService.
				saveGitRepositoryVersionKnowledgeModel(
						new GitRepositoryVersionKnowledgeModelForm1(form.getIdGitRepositoryVersion(), form.getKnowledgeMetric(), form.getFoldersPaths()));
		saveTruckFactor(gitRepositoryVersionKnowledgeModel.getId());
		return gitRepositoryVersionKnowledgeModel;
	}

	@Transactional
	public GitRepositoryVersion saveGitRepositoryVersionKnowledgeModelTruckFactorAllFolders(GitRepositoryVersion gitRepositoryVersion, KnowledgeModel knowledgeModel) throws Exception{
		List<GitRepositoryFolder> folders = new ArrayList<>();
		getAllFolders(gitRepositoryVersion.getRootFolder(), folders);
		List<String> paths = folders.stream().map(f -> f.getPath()).toList();
		for (String path : paths) {
			List<String> foldersPath = new ArrayList<>();
			if(path != null) {
				foldersPath.add(path);
			}
			saveGitRepositoryVersionKnowledgeTruckFactor(GitRepositoryVersionKnowledgeModelForm1.builder()
					.idGitRepositoryVersion(gitRepositoryVersion.getId())
					.knowledgeMetric(knowledgeModel).foldersPaths(foldersPath).build());
		}
		GitRepositoryVersion version = gitRepositoryVersionRepository.findById(gitRepositoryVersion.getId()).get();
		return version;
	}

	private void getAllFolders(GitRepositoryFolder folder, List<GitRepositoryFolder> allFolders){
		allFolders.add(folder);
		if (folder.getChildren() != null) {
			for (GitRepositoryFolder child : folder.getChildren()) {
				getAllFolders(child, allFolders);
			}
		}
	}
}
