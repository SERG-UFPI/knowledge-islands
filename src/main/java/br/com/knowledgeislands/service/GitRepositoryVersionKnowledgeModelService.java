package br.com.knowledgeislands.service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import br.com.knowledgeislands.dto.CreateAuthorFileExpertiseDTO;
import br.com.knowledgeislands.dto.form.GitRepositoryVersionKnowledgeModelForm1;
import br.com.knowledgeislands.dto.form.GitRepositoryVersionKnowledgeModelForm2;
import br.com.knowledgeislands.exceptions.MachineLearningUseException;
import br.com.knowledgeislands.model.entity.AuthorFileExpertise;
import br.com.knowledgeislands.model.entity.Commit;
import br.com.knowledgeislands.model.entity.CommitFile;
import br.com.knowledgeislands.model.entity.Contributor;
import br.com.knowledgeislands.model.entity.ContributorVersion;
import br.com.knowledgeislands.model.entity.DOA;
import br.com.knowledgeislands.model.entity.DOE;
import br.com.knowledgeislands.model.entity.File;
import br.com.knowledgeislands.model.entity.FileVersion;
import br.com.knowledgeislands.model.entity.GitRepository;
import br.com.knowledgeislands.model.entity.GitRepositoryFolder;
import br.com.knowledgeislands.model.entity.GitRepositoryVersion;
import br.com.knowledgeislands.model.entity.GitRepositoryVersionKnowledgeModel;
import br.com.knowledgeislands.model.entity.GitRepositoryVersionKnowledgeModelGenAi;
import br.com.knowledgeislands.model.enums.KnowledgeModel;
import br.com.knowledgeislands.model.enums.OperationType;
import br.com.knowledgeislands.model.vo.MlOutput;
import br.com.knowledgeislands.repository.GitRepositoryFolderRepository;
import br.com.knowledgeislands.repository.GitRepositoryRepository;
import br.com.knowledgeislands.repository.GitRepositoryVersionKnowledgeModelRepository;
import br.com.knowledgeislands.repository.GitRepositoryVersionRepository;
import br.com.knowledgeislands.repository.SharedLinkCommitRepository;
import br.com.knowledgeislands.utils.KnowledgeIslandsUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class GitRepositoryVersionKnowledgeModelService {

	@Autowired
	private GitRepositoryVersionKnowledgeModelRepository gitRepositoryVersionKnowledgeModelRepository;
	@Autowired
	private GitRepositoryVersionRepository gitRepositoryVersionRepository;
	@Autowired
	private GitRepositoryFolderRepository gitRepositoryFolderRepository;
	@Autowired
	private GitRepositoryVersionKnowledgeModelGenAiService gitRepositoryVersionKnowledgeModelGenAiService;
	@Autowired
	private GitRepositoryRepository gitRepositoryRepository;
	@Autowired
	private SharedLinkCommitRepository sharedLinkCommitRepository;
	@Value("${configuration.csv-input-ml.path}")
	private String csvMlInput;
	@Value("${configuration.csv-output-ml.path}")
	private String csvMlOutput;
	private String[] header = new String[] {"Adds", "QuantDias", "TotalLinhas", "PrimeiroAutor", "Author", "File"};

	public GitRepositoryVersionKnowledgeModelForm1 convertModelForm1ModelForm2(GitRepositoryVersionKnowledgeModelForm2 form) {
		GitRepositoryVersionKnowledgeModelForm1 form1  = new GitRepositoryVersionKnowledgeModelForm1();
		form1.setIdGitRepositoryVersion(form.getIdGitRepositoryVersion());
		form1.setKnowledgeMetric(form.getKnowledgeMetric());
		if(form.getFoldersIds() != null && !form.getFoldersIds().isEmpty()) {
			List<GitRepositoryFolder> folders = gitRepositoryFolderRepository.findAllById(form.getFoldersIds());
			form1.setFoldersPaths(folders.stream().map(f -> f.getPath()).filter(f -> f!=null).toList());
		}
		return form1;
	}

	@Transactional
	public GitRepositoryVersionKnowledgeModel saveGitRepositoryVersionKnowledgeModel(GitRepositoryVersionKnowledgeModelForm1 form) throws MachineLearningUseException {
		GitRepositoryVersion gitRepositoryVersion = gitRepositoryVersionRepository.findById(form.getIdGitRepositoryVersion()).get();
		if((form.getModelGenAi() != null && gitRepositoryVersionKnowledgeModelRepository
				.existsByRepositoryVersionIdAndKnowledgeModelAndGitRepositoryVersionKnowledgeModelGenAiAvgPctFilesGenAi(gitRepositoryVersion.getId(), form.getKnowledgeMetric(), form.getModelGenAi().getAvgPctFilesGenAi()))
				|| (form.getModelGenAi() == null && gitRepositoryVersionKnowledgeModelRepository
				.existsByRepositoryVersionIdAndKnowledgeModelAndGitRepositoryVersionKnowledgeModelGenAiIsNull(gitRepositoryVersion.getId(), form.getKnowledgeMetric()))) {
			log.warn("GitRepositoryVersionKnowledgeModel already created");
			return null;
		}
		log.info("====== BEGIN SAVING MODEL FOR "+gitRepositoryVersion.getGitRepository().getFullName());
		Collections.sort(gitRepositoryVersion.getCommits());
		GitRepositoryVersionKnowledgeModel gitRepositoryVersionKnowledgeModel = new GitRepositoryVersionKnowledgeModel(gitRepositoryVersion, form.getKnowledgeMetric(), 
				form.getFoldersPaths(), form.getModelGenAi());
		List<FileVersion> filesVersion = new ArrayList<>();
		if(form.getFoldersPaths() == null || form.getFoldersPaths().isEmpty()){
			gitRepositoryVersion.getFiles().stream().forEach(f -> filesVersion.add(new FileVersion(f)));
		}else {
			for (File file : gitRepositoryVersion.getFiles()) {
				for (String path : form.getFoldersPaths()) {
					if(file.getPath() != null && file.getPath().startsWith(path)) {
						filesVersion.add(new FileVersion(file));
						break;
					}
				}
			}
		}
		List<ContributorVersion> contributorsVersion = gitRepositoryVersion.getContributors().stream().map(ContributorVersion::new).toList();
		List<AuthorFileExpertise> authorFiles = new ArrayList<>();
		Map<String, String> fileFirstAuthorMap = new HashMap<>();
		Map<String, List<Commit>> fileCommitsMap = new HashMap<>();
		Map<String, FileVersion> fileMap = createFileVersionMap(filesVersion);
		log.info("Setting expertise models...");
		for(ContributorVersion contributorVersion: contributorsVersion) {
			List<File> filesContributor = filesTouchedByContributor(contributorVersion.getContributor(), gitRepositoryVersion.getCommits());
			Collections.shuffle(filesContributor);
			int fileToProcess = getIntFilesToProcess(gitRepositoryVersionKnowledgeModel, filesContributor);
			int genAiCounter = 0;
			for (File fileContributor: filesContributor) {
				FileVersion fileVersion = fileMap.get(fileContributor.getPath());
				if(fileVersion != null) {
					String fileVersionPath = fileVersion.getFile().getPath();
					List<Commit> commits = fileCommitsMap.get(fileVersionPath);
					if(commits == null) {
						commits = getCommitsFile(gitRepositoryVersion.getCommits(), fileVersion.getFile());
						fileCommitsMap.put(fileVersionPath, commits);
					}
					CreateAuthorFileExpertiseDTO createAuthorFileExpertiseDTO = CreateAuthorFileExpertiseDTO.builder().knowledgeMetric(form.getKnowledgeMetric())
							.commits(commits).contributorVersion(contributorVersion).fileVersion(fileVersion)
							.genAi(fileToProcess != -1 && genAiCounter < fileToProcess).build();
					AuthorFileExpertise authorFile = getAuthorFileByKnowledgeMetric(createAuthorFileExpertiseDTO, fileFirstAuthorMap, gitRepositoryVersion.getDateVersion());
					addToFileTotalKnowledge(form.getKnowledgeMetric(), fileVersion, authorFile);
					authorFiles.add(authorFile);
					genAiCounter++;
				}
			}
		}
		log.info("Setting authorships...");
		roundTotalKnowledgeFilesVersion(filesVersion);
		gitRepositoryVersionKnowledgeModel.setFiles(filesVersion);
		setContributorExpertiseData(contributorsVersion, authorFiles, gitRepositoryVersion.getFiles(), form.getKnowledgeMetric(), gitRepositoryVersion.getNumberAnalysedFiles());
		gitRepositoryVersionKnowledgeModel.setContributors(contributorsVersion);
		gitRepositoryVersionKnowledgeModel.setAuthorsFiles(authorFiles);
		log.info("Saving data...");
		gitRepositoryVersionKnowledgeModelRepository.save(gitRepositoryVersionKnowledgeModel);
		log.info("====== ENDING SAVING MODEL FOR "+gitRepositoryVersion.getGitRepository().getFullName());
		return gitRepositoryVersionKnowledgeModel;
	}

	private int getIntFilesToProcess(GitRepositoryVersionKnowledgeModel gitRepositoryVersionKnowledgeModel, List<File> filesContributor) {
		int fileToProcess = -1;
		if(gitRepositoryVersionKnowledgeModel.getGitRepositoryVersionKnowledgeModelGenAi() != null) {
			fileToProcess = KnowledgeIslandsUtils.getIntFromPercentage(filesContributor.size(), 
					gitRepositoryVersionKnowledgeModel.getGitRepositoryVersionKnowledgeModelGenAi().getAvgPctFilesGenAi());
		}
		return fileToProcess;
	}

	private Map<String, FileVersion> createFileVersionMap(List<FileVersion> filesVersion) {
		Map<String, FileVersion> fileMap = new HashMap<>();
		for (FileVersion fileVersion : filesVersion) {
			fileMap.put(fileVersion.getFile().getPath(), fileVersion);
			for (String renamePath : fileVersion.getFile().getRenamePaths()) {
				fileMap.put(renamePath, fileVersion);
			}
		}
		return fileMap;
	}

	private List<Commit> getCommitsFile(List<Commit> commits, File file) {
		Set<String> filePaths = file.getFilePaths();
		List<Commit> commitsFile = new ArrayList<>();
		for (Commit commit : commits) {
			for (CommitFile commitFile: commit.getCommitFiles()) {
				if (filePaths.contains(commitFile.getFile().getPath())) {
					commitsFile.add(commit);
					break;
				}
			}
		}
		return commitsFile;
	}

	private void roundTotalKnowledgeFilesVersion(List<FileVersion> filesVersion) {
		for(FileVersion version: filesVersion) {
			if(version.getTotalKnowledge() > 0) {
				version.setTotalKnowledge(KnowledgeIslandsUtils.roundValue(version.getTotalKnowledge()));
			}
		}
	}

	private void addFileToFilesAuthor(List<ContributorVersion> contributorsVersion, AuthorFileExpertise authorFile, File file) {
		for (ContributorVersion contributorVersion: contributorsVersion) {
			if (authorFile.getContributorVersion().getContributor().equals(contributorVersion.getContributor())) {
				contributorVersion.getFilesAuthor().add(file);
				contributorVersion.setNumberFilesAuthor(contributorVersion.getNumberFilesAuthor()+1);
				break;
			}
		}
	}

	private void setContributorExpertiseData(List<ContributorVersion> contributorsVersion, 
			List<AuthorFileExpertise> authorsFiles, List<File> files, KnowledgeModel knowledgeMetric, int numberAnalysedFiles) throws MachineLearningUseException {
		if (!knowledgeMetric.equals(KnowledgeModel.MACHINE_LEARNING)) {
			for (File file: files) {
				List<AuthorFileExpertise> authorsFile = authorsFiles.stream().
						filter(authorFile -> authorFile.getFileVersion().getFile().getPath().equals(file.getPath())).toList();
				if(authorsFile != null && !authorsFile.isEmpty()) {
					double maxValue = 0.0;
					for (AuthorFileExpertise authorFile : authorsFile) {
						if(knowledgeMetric.equals(KnowledgeModel.DOE) && authorFile.getDoe().getDoeValue() > maxValue) {
							maxValue = authorFile.getDoe().getDoeValue();
						}else if(knowledgeMetric.equals(KnowledgeModel.DOA) && authorFile.getDoa().getDoaValue() > maxValue) {
							maxValue = authorFile.getDoa().getDoaValue();
						}
					}
					for (AuthorFileExpertise authorFile : authorsFile) {
						double normalized;
						if (knowledgeMetric.equals(KnowledgeModel.DOE)) {
							normalized = authorFile.getDoe().getDoeValue()/maxValue;
							if (normalized >= KnowledgeIslandsUtils.normalizedThresholdMantainerDOE 
									&& authorFile.getDoe().getAdds() > 0) {
								addFileToFilesAuthor(contributorsVersion, authorFile, file);
							}
						}else if(knowledgeMetric.equals(KnowledgeModel.DOA)){
							normalized = authorFile.getDoa().getDoaValue()/maxValue;
							if (normalized > KnowledgeIslandsUtils.normalizedThresholdMantainerDOA && 
									authorFile.getDoa().getDoaValue() >= KnowledgeIslandsUtils.thresholdMantainerDOA) {
								addFileToFilesAuthor(contributorsVersion, authorFile, file);
							}
						}
					}
				}
			}
		}else {
			java.io.File fileInput = new java.io.File(csvMlInput);
			FileWriter outputfile;
			try {
				outputfile = new FileWriter(fileInput);
				CSVWriter writer = new CSVWriter(outputfile);
				writer.writeNext(header);
				for (AuthorFileExpertise authorFile : authorsFiles) {
					writer.writeNext(new String[] {
							String.valueOf(authorFile.getDoe().getAdds()),
							String.valueOf(authorFile.getDoe().getNumDays()),
							String.valueOf(authorFile.getDoe().getSize()),
							String.valueOf(authorFile.getDoe().getFa()),
							authorFile.getContributorVersion().getContributor().getEmail(),
							authorFile.getFileVersion().getFile().getPath()
					});
				}
				writer.close();
				List<MlOutput> output = new ArrayList<>();
				CSVReader reader = new CSVReader(new FileReader(csvMlOutput));
				String pathsRScript = GitRepositoryVersionKnowledgeModelService.class.getResource("/models_scripts_r/predictionScript.R").toURI().getPath();
				String pathRds = GitRepositoryVersionKnowledgeModelService.class.getResource("/models_scripts_r/final_model.rds").toURI().getPath();
				ProcessBuilder pb = new ProcessBuilder("/usr/bin/Rscript", pathsRScript, csvMlInput, csvMlOutput, pathRds);
				pb.redirectOutput(Redirect.INHERIT);
				pb.redirectError(Redirect.INHERIT);
				Process process = pb.start();
				process.waitFor();
				String[] lineInArray;
				while ((lineInArray = reader.readNext()) != null) {
					output.add(new MlOutput(lineInArray[5], lineInArray[6], lineInArray[7]));
				}
				output.remove(0);
				for (File file: files) {
					Set<String> filePaths = file.getFilePaths();
					for (MlOutput mlOutput : output) {
						if(filePaths.contains(mlOutput.getFile()) && mlOutput.getExpertise().equals(KnowledgeIslandsUtils.mantenedor)) {
							for (ContributorVersion contributorKnowledgeModel: contributorsVersion) {
								if (contributorKnowledgeModel.getContributor().getEmail().equals(mlOutput.getAuthor())) {
									contributorKnowledgeModel.getFilesAuthor().add(file);
									contributorKnowledgeModel.setNumberFilesAuthor(contributorKnowledgeModel.getNumberFilesAuthor()+1);
									break;
								}
							}
						}
					}
				}
				reader.close();
			} catch (IOException | InterruptedException | CsvValidationException | URISyntaxException e) {
				e.printStackTrace();
				log.error(e.getMessage());
				throw new MachineLearningUseException();
			}
		}
		for (ContributorVersion contributorVersion : contributorsVersion) {
			if(contributorVersion.getNumberFilesAuthor() > 0) {
				contributorVersion.setPercentOfFilesAuthored(KnowledgeIslandsUtils.roundValue((double)contributorVersion.getNumberFilesAuthor()/numberAnalysedFiles));
			}
		}
	}

	private void addToFileTotalKnowledge(KnowledgeModel knowledgeMetric, FileVersion fileVersion, AuthorFileExpertise authorFile) {
		if (!knowledgeMetric.equals(KnowledgeModel.DOA)) {
			fileVersion.setTotalKnowledge(fileVersion.getTotalKnowledge()+authorFile.getDoe().getDoeValue());
		}else {
			fileVersion.setTotalKnowledge(fileVersion.getTotalKnowledge()+authorFile.getDoa().getDoaValue());
		}
	}

	private List<File> filesTouchedByContributor(Contributor contributor, List<Commit> commits) {
		if (contributor == null || commits == null) {
			throw new IllegalArgumentException("Contributor and commits must not be null.");
		}
		List<File> files = new ArrayList<>();
		Set<String> filePaths = new HashSet<>();
		List<Contributor> contributors = contributor.contributorAlias();
		for (Commit commit : commits) {
			if (contributors.contains(commit.getAuthor())) {
				for (CommitFile commitFile : commit.getCommitFiles()) {
					if (filePaths.add(commitFile.getFile().getPath())) {
						files.add(commitFile.getFile());
					}
				}
			}
		}
		return files;
	}

	private AuthorFileExpertise getAuthorFileByKnowledgeMetric(CreateAuthorFileExpertiseDTO dto, Map<String, String> fileFirstAuthorMap, Date versionDate) {
		if (!dto.getKnowledgeMetric().equals(KnowledgeModel.DOA)) {
			DOE doe = getDoeContributorFile(dto.getContributorVersion().getContributor(), dto.getFileVersion().getFile(), dto.getCommits(), dto.isGenAi(), fileFirstAuthorMap, versionDate);
			return new AuthorFileExpertise(dto.getContributorVersion(), dto.getFileVersion(), doe, dto.isGenAi());
		}else {
			DOA doa = getDoaContributorFile(dto.getContributorVersion().getContributor(), dto.getFileVersion().getFile(), dto.getCommits(), fileFirstAuthorMap);
			return new AuthorFileExpertise(dto.getContributorVersion(), dto.getFileVersion(), doa, dto.isGenAi());
		}
	}

	private int isContributorFa(List<Contributor> contributors, File file, List<Commit> commits, Map<String, String> fileFirstAuthorMap) {
		Set<String> filePaths = file.getFilePaths();
		String emailName = fileFirstAuthorMap.get(file.getPath());
		if(emailName != null) {
			List<String> emailsNames = contributors.stream().map(c -> c.getEmail()+c.getName()).toList();
			return emailsNames.contains(emailName) ? 1: 0;
		}
		for (Commit commit : commits) {
			for (CommitFile commitFile: commit.getCommitFiles()) {
				if(filePaths.contains(commitFile.getFile().getPath()) && 
						commitFile.getStatus().equals(OperationType.ADDED)) {
					if(!fileFirstAuthorMap.containsKey(file.getPath())) {
						fileFirstAuthorMap.put(file.getPath(), commit.getAuthor().getEmail()+commit.getAuthor().getName());
					}
					return contributors.contains(commit.getAuthor()) ? 1: 0;
				}
			}
		}
		return 0;
	}

	private DOE getDoeContributorFile(Contributor contributor, File file, List<Commit> commits, boolean genAi, 
			Map<String, String> fileFirstAuthorMap, Date versionDate) {
		Collections.sort(commits);
		List<Contributor> contributors = contributor.contributorAlias();
		int adds = 0;
		int fa = isContributorFa(contributors, file, commits, fileFirstAuthorMap);
		Date dateLastCommit = commits.get(0).getAuthorDate();
		Set<String> filePaths = file.getFilePaths();
		for (Commit commit : commits) {
			if (!contributors.contains(commit.getAuthor())) continue;
			if (commit.getAuthorDate().after(dateLastCommit)) {
				dateLastCommit = commit.getAuthorDate();
			}
			for (CommitFile commitFile : commit.getCommitFiles()) {
				if (!filePaths.contains(commitFile.getFile().getPath())) continue;
				adds += commitFile.getAdditions();
				break;
			}
		}
		int numDays = (int) TimeUnit.DAYS.convert(versionDate.getTime() - dateLastCommit.getTime(), TimeUnit.MILLISECONDS);
		if (genAi && contributor.getContributorGenAiUse() != null) {
			int toRemoveAdds = KnowledgeIslandsUtils.getIntFromPercentage(adds, contributor.getContributorGenAiUse().getAvgCopiedLinesCommits());
			adds = adds - toRemoveAdds;
		}
		double doeValue = KnowledgeIslandsUtils.getDOE(adds, fa, numDays, file.getSize());
		return new DOE(adds, fa, numDays, file.getSize(), KnowledgeIslandsUtils.roundValue(doeValue));
	}

	private DOA getDoaContributorFile(Contributor contributor, 
			File file, List<Commit> commits, Map<String, String> fileFirstAuthorMap) {
		List<Contributor> contributors = contributor.contributorAlias();
		int dl = 0;
		int ac = 0; 
		int fa = isContributorFa(contributors, file, commits, fileFirstAuthorMap);
		for (Commit commit : commits) {
			for (CommitFile commitFile: commit.getCommitFiles()) {
				if (commitFile.getFile().getPath().equals(file.getPath())) {
					if (contributors.contains(commit.getAuthor())) {
						dl = dl + 1;
					}else {
						ac = ac + 1;
					}
					break;
				}
			}
		}
		double doaValue = KnowledgeIslandsUtils.getDOA(fa, dl,ac);
		return new DOA(fa, dl, ac, doaValue);
	}

	public List<GitRepositoryVersionKnowledgeModel> getByGitRepositoryVersionId(Long id) {
		return gitRepositoryVersionKnowledgeModelRepository.findByRepositoryVersionId(id);
	}

	@Transactional
	public void saveRepositoryVersionKnowledgeSharedLinks(KnowledgeModel knowledgeMetric) {
		List<GitRepository> repositories = sharedLinkCommitRepository.findRepositoriesNotFilteredBySharedLinkCommitWithCommitFile();
		repositories = repositories.stream().filter(r -> !r.isFiltered()).toList();
		try {
			for (GitRepository gitRepository : repositories) {
				List<GitRepositoryVersion> versions = gitRepositoryVersionRepository.findByGitRepositoryId(gitRepository.getId());
				GitRepositoryVersion version = versions.get(0);
				saveGitRepositoryVersionKnowledgeModel(GitRepositoryVersionKnowledgeModelForm1.builder()
						.idGitRepositoryVersion(version.getId()).knowledgeMetric(knowledgeMetric).foldersPaths(null).build());
			}
		}catch (Exception e) {
			log.info(e.getMessage());
		}
	}

	@Transactional
	public void saveRepositoryVersionKnowledgeSharedLinksGenAi(KnowledgeModel knowledgeMetric) {
		List<GitRepositoryVersionKnowledgeModelGenAi> modelsGenAi = gitRepositoryVersionKnowledgeModelGenAiService.createGitRepositoryVersionKnowledgeModelGenAi();
		List<GitRepository> repositories = sharedLinkCommitRepository.findRepositoriesNotFilteredBySharedLinkCommitWithCommitFile();
		repositories = repositories.stream().filter(r -> !r.isFiltered()).toList();
		saveGitRepositoryVersionKnowledgeModelPercentages(modelsGenAi, repositories, knowledgeMetric);
	}

	public void saveRepositoryVersionKnowledgeGenAiByRepositoryId(Long idGitRepository) throws MachineLearningUseException {
		List<GitRepositoryVersionKnowledgeModelGenAi> modelsGenAi = gitRepositoryVersionKnowledgeModelGenAiService.createGitRepositoryVersionKnowledgeModelGenAi();
		GitRepository repository = gitRepositoryRepository.findById(idGitRepository).get();
		for (KnowledgeModel knowledgeMetric : Arrays.asList(KnowledgeModel.DOE, KnowledgeModel.MACHINE_LEARNING)) {
			saveGitRepositoryVersionKnowledgeModelPercentages(modelsGenAi, Arrays.asList(repository), knowledgeMetric);
			log.info("----- RAW ANALYSIS -"+ knowledgeMetric.name());
			List<GitRepositoryVersion> versions = gitRepositoryVersionRepository.findByGitRepositoryId(repository.getId());
			saveGitRepositoryVersionKnowledgeModel(GitRepositoryVersionKnowledgeModelForm1.builder()
					.idGitRepositoryVersion(versions.get(0).getId()).knowledgeMetric(knowledgeMetric).build());
		}
	}

	public void saveRepositoryVersionKnowledgeGenAi() throws MachineLearningUseException {
		List<GitRepositoryVersionKnowledgeModelGenAi> modelsGenAi = gitRepositoryVersionKnowledgeModelGenAiService.createGitRepositoryVersionKnowledgeModelGenAi();
		List<GitRepository> repositories = gitRepositoryRepository.findByFilteredFalse();
		for (KnowledgeModel knowledgeMetric : Arrays.asList(KnowledgeModel.DOE, KnowledgeModel.MACHINE_LEARNING)) {
			saveGitRepositoryVersionKnowledgeModelPercentages(modelsGenAi, repositories, knowledgeMetric);
			for (GitRepository gitRepository : repositories) {
				log.info("----- RAW ANALYSIS -"+ knowledgeMetric.name());
				List<GitRepositoryVersion> versions = gitRepositoryVersionRepository.findByGitRepositoryId(gitRepository.getId());
				saveGitRepositoryVersionKnowledgeModel(GitRepositoryVersionKnowledgeModelForm1.builder()
						.idGitRepositoryVersion(versions.get(0).getId()).knowledgeMetric(knowledgeMetric).build());
			}
		}
	}

	private void saveGitRepositoryVersionKnowledgeModelPercentages(List<GitRepositoryVersionKnowledgeModelGenAi> modelsGenAi,
			List<GitRepository> repositories, KnowledgeModel knowledgeMetric) {
		try {
			for (GitRepositoryVersionKnowledgeModelGenAi modelGenAi : modelsGenAi) {
				log.info("----- "+modelGenAi.getAvgPctFilesGenAi()+" PERCENTAGE ANALYSIS - "+knowledgeMetric.name());
				for (GitRepository gitRepository : repositories) {
					List<GitRepositoryVersion> versions = gitRepositoryVersionRepository.findByGitRepositoryId(gitRepository.getId());
					saveGitRepositoryVersionKnowledgeModel(GitRepositoryVersionKnowledgeModelForm1.builder()
							.idGitRepositoryVersion(versions.get(0).getId()).knowledgeMetric(knowledgeMetric).modelGenAi(modelGenAi)
							.build());
				}
			}
		}catch (Exception e) {
			log.info(e.getMessage());
		}
	}

	public void saveGitRepositoryVersionKnowledgeModelNotFiltered() throws Exception {
		List<GitRepository> repositories = gitRepositoryRepository.findByFilteredFalse();
		for (KnowledgeModel knowledgeMetric : Arrays.asList(KnowledgeModel.DOE, KnowledgeModel.MACHINE_LEARNING)) {
			for (GitRepository gitRepository : repositories) {
				List<GitRepositoryVersion> versions = gitRepositoryVersionRepository.findByGitRepositoryId(gitRepository.getId());
				saveGitRepositoryVersionKnowledgeModel(GitRepositoryVersionKnowledgeModelForm1.builder()
						.idGitRepositoryVersion(versions.get(0).getId()).knowledgeMetric(knowledgeMetric).build());
			}
		}
	}

	public void removeGitRepositoryVersionKnowledgeModel(Long id) {
		gitRepositoryVersionKnowledgeModelRepository.deleteById(id);
	}

	public void saveGitRepositoryVersionKnowledgeModelNotFilteredDOE() throws MachineLearningUseException {
		List<GitRepository> repositories = gitRepositoryRepository.findByFilteredFalse();
		for (GitRepository gitRepository : repositories) {
			List<GitRepositoryVersion> versions = gitRepositoryVersionRepository.findByGitRepositoryId(gitRepository.getId());
			saveGitRepositoryVersionKnowledgeModel(GitRepositoryVersionKnowledgeModelForm1.builder()
					.idGitRepositoryVersion(versions.get(0).getId()).knowledgeMetric(KnowledgeModel.DOE).build());
		}
	}

}
