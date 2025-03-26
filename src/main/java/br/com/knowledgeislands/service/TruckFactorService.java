package br.com.knowledgeislands.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import javax.persistence.EntityNotFoundException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.knowledgeislands.dto.form.CloneRepoForm;
import br.com.knowledgeislands.dto.form.GitRepositoryVersionKnowledgeModelForm1;
import br.com.knowledgeislands.dto.form.HistoryReposTruckFactorForm;
import br.com.knowledgeislands.dto.form.RepositoryKnowledgeMetricForm;
import br.com.knowledgeislands.extractors.HistoryCommitsExtractor;
import br.com.knowledgeislands.model.entity.ContributorVersion;
import br.com.knowledgeislands.model.entity.File;
import br.com.knowledgeislands.model.entity.FileVersion;
import br.com.knowledgeislands.model.entity.GitRepository;
import br.com.knowledgeislands.model.entity.GitRepositoryFolder;
import br.com.knowledgeislands.model.entity.GitRepositoryVersion;
import br.com.knowledgeislands.model.entity.GitRepositoryVersionKnowledgeModel;
import br.com.knowledgeislands.model.entity.GitRepositoryVersionProcess;
import br.com.knowledgeislands.model.entity.TruckFactor;
import br.com.knowledgeislands.model.enums.GitRepositoryVersionProcessStageEnum;
import br.com.knowledgeislands.model.enums.KnowledgeModel;
import br.com.knowledgeislands.repository.GitRepositoryVersionKnowledgeModelRepository;
import br.com.knowledgeislands.repository.GitRepositoryVersionProcessRepository;
import br.com.knowledgeislands.repository.GitRepositoryVersionRepository;
import br.com.knowledgeislands.repository.TruckFactorRepository;
import br.com.knowledgeislands.utils.KnowledgeIslandsUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
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
	@Value("${configuration.permanent-clone.path}")
	private String cloneFolder;

	@Async
	@Transactional
	public void continueProcesses(GitRepositoryVersionProcess process, CloneRepoForm form) {
		try {
			setProcessStage(process, GitRepositoryVersionProcessStageEnum.DOWNLOADING);
			String projectPath = downloaderService.cloneProject(form);
			setProcessStage(process, GitRepositoryVersionProcessStageEnum.EXTRACTING_DATA);
			gitRepositoryService.generateLogFiles(projectPath);
			GitRepositoryVersion gitRepositoryVersion = gitRepositoryVersionService.saveGitRepositoryAndGitRepositoryVersion(projectPath);
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

	public List<TruckFactor> generateLogsTruckFactorRepository(RepositoryKnowledgeMetricForm form) throws Exception {
		gitRepositoryService.generateLogFiles(form.getRepositoryPath());
		return generateTruckFactorRepository(form);
	}

	@Transactional
	public TruckFactor saveTruckFactor(Long idGitRepositoryVersionKnowledgeModel) {
		long start = System.currentTimeMillis();
		GitRepositoryVersionKnowledgeModel gitRepositoryVersionKnowledgeModel = gitRepositoryVersionKnowledgeModelRepository.findById(idGitRepositoryVersionKnowledgeModel)
				.orElseThrow(()->new EntityNotFoundException("GitRepositoryVersionKnowledgeModel not found with id: " + idGitRepositoryVersionKnowledgeModel));
		log.info("====== BEGIN SAVING TRUCK FACTOR FOR "+gitRepositoryVersionKnowledgeModel.getRepositoryVersion().getGitRepository().getFullName()+" MODEL "+gitRepositoryVersionKnowledgeModel.getKnowledgeModel());
		gitRepositoryVersionKnowledgeModel.getContributors().removeIf(ckm -> ckm.getNumberFilesAuthor() == 0);
		Collections.sort(gitRepositoryVersionKnowledgeModel.getContributors(), Collections.reverseOrder());
		List<ContributorVersion> topContributors = new ArrayList<>();
		double fileSize = gitRepositoryVersionKnowledgeModel.getFiles().size();
		int tf = 0;
		while(!gitRepositoryVersionKnowledgeModel.getContributors().isEmpty()) {
			double numberFilesCovarage = getCoverageFiles(gitRepositoryVersionKnowledgeModel.getContributors(), gitRepositoryVersionKnowledgeModel.getFiles()).size();
			double coverage = numberFilesCovarage/fileSize;
			if(coverage < KnowledgeIslandsUtils.TRUCKFACTOR_COVERAGE_THRESHOLD) 
				break;
			topContributors.add(gitRepositoryVersionKnowledgeModel.getContributors().get(0));
			gitRepositoryVersionKnowledgeModel.getContributors().remove(0);
			tf = tf+1;
		}
		List<FileVersion> coveredFiles = getCoverageFiles(topContributors, gitRepositoryVersionKnowledgeModel.getFiles());
		double sec = (System.currentTimeMillis() - start) / 1000.0;
		TruckFactor truckFactor = new TruckFactor(tf, gitRepositoryVersionKnowledgeModel, coveredFiles, topContributors, sec);
		truckFactorRepository.save(truckFactor);
		gitRepositoryVersionKnowledgeModel.setTruckFactor(truckFactor);
		gitRepositoryVersionKnowledgeModelRepository.save(gitRepositoryVersionKnowledgeModel);
		log.info("====== ENDING SAVING TRUCK FACTOR FOR "+gitRepositoryVersionKnowledgeModel.getRepositoryVersion().getGitRepository().getFullName()+" MODEL "+gitRepositoryVersionKnowledgeModel.getKnowledgeModel());
		return truckFactor;
	}

	public List<TruckFactor> generateTruckFactorRepository(RepositoryKnowledgeMetricForm repo)
			throws Exception {
		GitRepositoryVersion gitRepositoryVersion = gitRepositoryVersionService.saveGitRepositoryAndGitRepositoryVersion(repo.getRepositoryPath());
		List<GitRepositoryVersionKnowledgeModel> models = new ArrayList<>();
		models.add(gitRepositoryVersionKnowledgeModelService.
				saveGitRepositoryVersionKnowledgeModel(new GitRepositoryVersionKnowledgeModelForm1(gitRepositoryVersion.getId(), repo.getKnowledgeMetric(), repo.getFoldersPaths(), null)));
		List<TruckFactor> truckFactors = new ArrayList<>();
		for(GitRepositoryVersionKnowledgeModel model: models) {
			truckFactors.add(saveTruckFactor(model.getId()));
		}
		return truckFactors;
	}

	public void directoriesTruckFactorAnalyzes(RepositoryKnowledgeMetricForm request) throws IOException, NoHeadException, GitAPIException{
		java.io.File dir = new java.io.File(request.getRepositoryPath());
		for (java.io.File fileDir : dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath() + "/";
				GitRepository project = gitRepositoryService.returnProjectByPath(projectPath);
				if (project != null && !project.isFiltered()) {
					try {
						RepositoryKnowledgeMetricForm repo = new RepositoryKnowledgeMetricForm(projectPath, request.getKnowledgeMetric());
						generateTruckFactorRepository(repo);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private List<FileVersion> getCoverageFiles(List<ContributorVersion> contributorsVersion, List<FileVersion> filesVersion) {
		List<FileVersion> files = new ArrayList<>();
		Set<String> contributorFiles = new HashSet<>();
		for (ContributorVersion contributor : contributorsVersion) {
			contributorFiles.addAll(contributor.getFilesAuthor().stream().map(File::getPath).toList());
		}
		for(FileVersion file: filesVersion) {
			Set<String> filePaths = file.getFile().getFilePaths();
			if(!Collections.disjoint(filePaths, contributorFiles)) {
				files.add(file);
			}
		}
		return files;
	}

	public void historyReposTruckFactor(HistoryReposTruckFactorForm form) {
		ExecutorService executorService = KnowledgeIslandsUtils.getExecutorServiceForTf();
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
						new GitRepositoryVersionKnowledgeModelForm1(form.getIdGitRepositoryVersion(), form.getKnowledgeMetric(), form.getFoldersPaths(), null));
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

	public void removeAll() {
		truckFactorRepository.deleteAll();
	}

	public void saveAllTruckFactor() {
		List<Long> idsModels = gitRepositoryVersionKnowledgeModelRepository.findAllIds();
		for (Long id : idsModels) {
			saveTruckFactor(id);
		}
	}

	public void saveAllTruckFactorIsNull() {
		List<Long> idsModels = gitRepositoryVersionKnowledgeModelRepository.findIdByTruckFactorIsNull();
		for (Long id : idsModels) {
			saveTruckFactor(id);
		}
	}
}
