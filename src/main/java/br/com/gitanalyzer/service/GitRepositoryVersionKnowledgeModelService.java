package br.com.gitanalyzer.service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import br.com.gitanalyzer.dto.CreateAuthorFileExpertiseDTO;
import br.com.gitanalyzer.dto.form.GitRepositoryVersionKnowledgeModelForm1;
import br.com.gitanalyzer.dto.form.GitRepositoryVersionKnowledgeModelForm2;
import br.com.gitanalyzer.model.entity.AuthorFileExpertise;
import br.com.gitanalyzer.model.entity.Commit;
import br.com.gitanalyzer.model.entity.CommitFile;
import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.ContributorVersion;
import br.com.gitanalyzer.model.entity.DOA;
import br.com.gitanalyzer.model.entity.DOE;
import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.FileVersion;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.entity.GitRepositoryFolder;
import br.com.gitanalyzer.model.entity.GitRepositoryVersion;
import br.com.gitanalyzer.model.entity.GitRepositoryVersionKnowledgeModel;
import br.com.gitanalyzer.model.entity.GitRepositoryVersionKnowledgeModelGenAi;
import br.com.gitanalyzer.model.enums.KnowledgeModel;
import br.com.gitanalyzer.model.enums.OperationType;
import br.com.gitanalyzer.model.vo.MlOutput;
import br.com.gitanalyzer.repository.GitRepositoryFolderRepository;
import br.com.gitanalyzer.repository.GitRepositoryVersionKnowledgeModelRepository;
import br.com.gitanalyzer.repository.GitRepositoryVersionRepository;
import br.com.gitanalyzer.repository.SharedLinkCommitRepository;
import br.com.gitanalyzer.utils.KnowledgeIslandsUtils;
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
	private SharedLinkCommitRepository sharedLinkCommitRepository;
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

	public GitRepositoryVersionKnowledgeModel saveGitRepositoryVersionKnowledgeModel(GitRepositoryVersionKnowledgeModelForm1 form) throws Exception {
		GitRepositoryVersion gitRepositoryVersion = gitRepositoryVersionRepository.findById(form.getIdGitRepositoryVersion()).get();
		log.info("BEGIN SAVING MODEL FOR "+gitRepositoryVersion.getGitRepository().getFullName());
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
		for(ContributorVersion contributorVersion: contributorsVersion) {
			List<File> filesContributor = filesTouchedByContributor(contributorVersion.getContributor(), gitRepositoryVersion.getCommits());
			Collections.shuffle(filesContributor);
			int fileToProcess = -1;
			if(gitRepositoryVersionKnowledgeModel.getGitRepositoryVersionKnowledgeModelGenAi() != null) {
				fileToProcess = KnowledgeIslandsUtils.getIntFromPercentage(filesContributor.size(), 
						gitRepositoryVersionKnowledgeModel.getGitRepositoryVersionKnowledgeModelGenAi().getAvgPctFilesGenAi());
			}
			int genAiCounter = 0;
			for (File fileContributor : filesContributor) {
				for (FileVersion fileVersion : filesVersion) {
					if(fileVersion.getFile().isFile(fileContributor.getPath())) {
						List<Commit> commitsFiles = getCommitsFile(gitRepositoryVersion.getCommits(), fileVersion.getFile());
						CreateAuthorFileExpertiseDTO createAuthorFileExpertiseDTO = CreateAuthorFileExpertiseDTO.builder().knowledgeMetric(form.getKnowledgeMetric())
								.commits(commitsFiles).contributorVersion(contributorVersion).fileVersion(fileVersion)
								.genAi(fileToProcess != -1 && genAiCounter < fileToProcess).build();
						AuthorFileExpertise authorFile = getAuthorFileByKnowledgeMetric(createAuthorFileExpertiseDTO);
						addToFileTotalKnowledge(form.getKnowledgeMetric(), fileVersion, authorFile);
						authorFiles.add(authorFile);
						genAiCounter++;
						break;
					}
				}
			}
		}
		roundTotalKnowledgeFilesVersion(filesVersion);
		gitRepositoryVersionKnowledgeModel.setFiles(filesVersion);
		setContributorExpertiseData(contributorsVersion, authorFiles, gitRepositoryVersion.getFiles(), form.getKnowledgeMetric(), gitRepositoryVersion.getNumberAnalysedFiles());
		gitRepositoryVersionKnowledgeModel.setContributors(contributorsVersion);
		gitRepositoryVersionKnowledgeModel.setAuthorsFiles(authorFiles);
		gitRepositoryVersionKnowledgeModelRepository.save(gitRepositoryVersionKnowledgeModel);
		log.info("ENDING SAVING MODEL FOR "+gitRepositoryVersion.getGitRepository().getFullName());
		return gitRepositoryVersionKnowledgeModel;
	}

	private List<Commit> getCommitsFile(List<Commit> commits, File file) {
		List<Commit> commitsFile = new ArrayList<>();
		for (Commit commit : commits) {
			for (CommitFile commitFile: commit.getCommitFiles()) {
				if (commitFile.getFile().isFile(file.getPath())) {
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
				version.setTotalKnowledge(returnRoundedValue(version.getTotalKnowledge()));
			}
		}
	}

	private double returnRoundedValue(double value) {
		BigDecimal bd = BigDecimal.valueOf(value);
		BigDecimal rounded = bd.setScale(4, RoundingMode.HALF_UP);
		return rounded.doubleValue();
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
			List<AuthorFileExpertise> authorsFiles, List<File> files, KnowledgeModel knowledgeMetric, int numberAnalysedFiles) {
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
							if (normalized >= KnowledgeIslandsUtils.normalizedThresholdMantainerDOE) {
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
			java.io.File fileInput = new java.io.File(KnowledgeIslandsUtils.pathInputMlFile);
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
			} catch (IOException e) {
				e.printStackTrace();
			}
			List<MlOutput> output = new ArrayList<>();
			try {
				ProcessBuilder pb = new ProcessBuilder("/usr/bin/Rscript", KnowledgeIslandsUtils.pathScriptMlFile);
				pb.redirectOutput(Redirect.INHERIT);
				pb.redirectError(Redirect.INHERIT);
				Process process = pb.start();
				process.waitFor();
				CSVReader reader = new CSVReader(new FileReader(KnowledgeIslandsUtils.pathOutputMlFile));
				String[] lineInArray;
				while ((lineInArray = reader.readNext()) != null) {
					output.add(new MlOutput(lineInArray[5], lineInArray[6], lineInArray[7]));
				}
				output.remove(0);
			} catch (IOException | InterruptedException | CsvValidationException e) {
				e.printStackTrace();
				log.info(e.getMessage());
			}
			for (File file: files) {
				for (MlOutput mlOutput : output) {
					if(file.isFile(mlOutput.getFile()) && mlOutput.getExpertise().equals(KnowledgeIslandsUtils.mantenedor)) {
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
		}
		for (ContributorVersion contributorVersion : contributorsVersion) {
			if(contributorVersion.getNumberFilesAuthor() > 0) {
				contributorVersion.setPercentOfFilesAuthored(returnRoundedValue((double)contributorVersion.getNumberFilesAuthor()/numberAnalysedFiles));
			}
		}
	}

	private void addToFileTotalKnowledge(KnowledgeModel knowledgeMetric, FileVersion fileVersion, AuthorFileExpertise authorFile) {
		if (knowledgeMetric.equals(KnowledgeModel.DOE) 
				|| knowledgeMetric.equals(KnowledgeModel.MACHINE_LEARNING)) {
			fileVersion.setTotalKnowledge(fileVersion.getTotalKnowledge()+authorFile.getDoe().getDoeValue());
		}else {
			fileVersion.setTotalKnowledge(fileVersion.getTotalKnowledge()+authorFile.getDoa().getDoaValue());
		}
	}

	private List<File> filesTouchedByContributor(Contributor contributor, List<Commit> commits){
		List<File> files = new ArrayList<>();
		List<Contributor> contributors = contributor.contributorAlias();
		for (Commit commit : commits) {
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					for (CommitFile commitFile: commit.getCommitFiles()) {
						if(files.stream().noneMatch(f -> f.isFile(commitFile.getFile().getPath()))) {
							files.add(commitFile.getFile());
						}
					}
					break;
				}
			}
		}
		return files;
	}

	private AuthorFileExpertise getAuthorFileByKnowledgeMetric(CreateAuthorFileExpertiseDTO dto) {
		if (!dto.getKnowledgeMetric().equals(KnowledgeModel.DOA)) {
			DOE doe = getDoeContributorFile(dto.getContributorVersion().getContributor(), dto.getFileVersion().getFile(), dto.getCommits(), dto.isGenAi());
			return new AuthorFileExpertise(dto.getContributorVersion(), dto.getFileVersion(), doe, dto.isGenAi());
		}else {
			DOA doa = getDoaContributorFile(dto.getContributorVersion().getContributor(), dto.getFileVersion().getFile(), dto.getCommits());
			return new AuthorFileExpertise(dto.getContributorVersion(), dto.getFileVersion(), doa, dto.isGenAi());
		}
	}

	private int isContributorFa(List<Contributor> contributors, File file, List<Commit> commits) {
		for (Commit commit : commits) {
			for (CommitFile commitFile: commit.getCommitFiles()) {
				if(file.isFile(commitFile.getFile().getPath()) && 
						commitFile.getStatus().equals(OperationType.ADDED)) {
					if(contributors.contains(commit.getAuthor())) {
						return 1;
					}
					return 0;
				}
			}
		}
		return 0;
	}

	private DOE getDoeContributorFile(Contributor contributor, File file, List<Commit> commits, boolean genAi) {
		List<Contributor> contributors = contributor.contributorAlias();
		Date currentDate = new Date();
		int adds = 0;
		int fa = isContributorFa(contributors, file, commits);
		Date dateLastCommit = commits.get(0).getAuthorDate();
		for (Commit commit : commits) {
			if (!contributors.contains(commit.getAuthor())) continue;
			for (CommitFile commitFile : commit.getCommitFiles()) {
				if (!file.isFile(commitFile.getFile().getPath())) continue;
				adds += commitFile.getAdditions();
				if (commit.getAuthorDate().after(dateLastCommit)) {
					dateLastCommit = commit.getAuthorDate();
				}
				break;
			}
		}
		int numDays = (int) TimeUnit.DAYS.convert(currentDate.getTime() - dateLastCommit.getTime(), TimeUnit.MILLISECONDS);
		if (genAi && contributor.getContributorGenAiUse() != null) {
			int toRemoveAdds = KnowledgeIslandsUtils.getIntFromPercentage(adds, contributor.getContributorGenAiUse().getAvgCopiedLinesCommits());
			adds = adds - toRemoveAdds;
		}
		double doeValue = KnowledgeIslandsUtils.getDOE(adds, fa, numDays, file.getSize());
		return new DOE(adds, fa, numDays, file.getSize(), doeValue);
	}

	private DOA getDoaContributorFile(Contributor contributor, 
			File file, List<Commit> commits) {
		List<Contributor> contributors = contributor.contributorAlias();
		int dl = 0;
		int ac = 0; 
		int fa = isContributorFa(contributors, file, commits);
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
		try {
			for (GitRepositoryVersionKnowledgeModelGenAi modelGenAi : modelsGenAi) {
				log.info("----- "+modelGenAi.getAvgPctFilesGenAi()+" PERCENTAGE ANALYSIS");
				for (GitRepository gitRepository : repositories) {
					List<GitRepositoryVersion> versions = gitRepositoryVersionRepository.findByGitRepositoryId(gitRepository.getId());
					GitRepositoryVersion version = versions.get(0);
					saveGitRepositoryVersionKnowledgeModel(GitRepositoryVersionKnowledgeModelForm1.builder()
							.idGitRepositoryVersion(version.getId()).knowledgeMetric(knowledgeMetric).foldersPaths(null).modelGenAi(modelGenAi)
							.build());
				}
			}
		}catch (Exception e) {
			log.info(e.getMessage());
		}
	}

	//	public void saveRepositoryVersionKnowledgeSharedLinksGenAiFull() {
	//		log.info("------ CREATING VERSION MODELS WITHOUT GEN-AI IMPACT");
	//		saveRepositoryVersionKnowledgeSharedLinks();
	//		log.info("------ CREATING VERSION MODELS WITH GEN-AI IMPACT");
	//		saveRepositoryVersionKnowledgeSharedLinksGenAi();
	//	}

}
