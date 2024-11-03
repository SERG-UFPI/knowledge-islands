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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

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
import br.com.gitanalyzer.model.entity.SharedLinkCommit;
import br.com.gitanalyzer.model.enums.KnowledgeModel;
import br.com.gitanalyzer.model.enums.OperationType;
import br.com.gitanalyzer.model.vo.MlOutput;
import br.com.gitanalyzer.repository.GitRepositoryFolderRepository;
import br.com.gitanalyzer.repository.GitRepositoryVersionKnowledgeModelRepository;
import br.com.gitanalyzer.repository.GitRepositoryVersionRepository;
import br.com.gitanalyzer.repository.SharedLinkCommitRepository;
import br.com.gitanalyzer.utils.DoaUtils;
import br.com.gitanalyzer.utils.DoeUtils;
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
	private SharedLinkCommitRepository sharedLinkCommitRepository;
	private DoeUtils doeUtils = new DoeUtils();
	private DoaUtils doaUtils = new DoaUtils();
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
	public GitRepositoryVersionKnowledgeModel saveGitRepositoryVersionKnowledgeModel(GitRepositoryVersionKnowledgeModelForm1 form) throws Exception {
		GitRepositoryVersion gitRepositoryVersion = gitRepositoryVersionRepository.findById(form.getIdGitRepositoryVersion()).get();
		Collections.sort(gitRepositoryVersion.getCommits());
		List<AuthorFileExpertise> authorFiles = new ArrayList<>();
		GitRepositoryVersionKnowledgeModel gitRepositoryVersionKnowledgeModel = new GitRepositoryVersionKnowledgeModel(gitRepositoryVersion, form.getKnowledgeMetric(), form.getFoldersPaths());
		List<FileVersion> filesVersion = new ArrayList<>();
		if(form.getFoldersPaths() == null || form.getFoldersPaths().isEmpty()){
			gitRepositoryVersion.getFiles().stream().forEach(f -> filesVersion.add(new FileVersion(f)));
		}else {
			List<File> files = new ArrayList<>();
			for (File file : gitRepositoryVersion.getFiles()) {
				for (String path : form.getFoldersPaths()) {
					if(file.getPath() != null && file.getPath().startsWith(path)) {
						filesVersion.add(new FileVersion(file));
						break;
					}
				}
			}
			files.forEach(f -> filesVersion.add(new FileVersion(f)));
		}
		List<ContributorVersion> contributorsVersion = gitRepositoryVersion.getContributors().stream().map(c -> new ContributorVersion(c)).toList();
		for(ContributorVersion contributorVersion: contributorsVersion) {
			List<File> filesContributor = filesTouchedByContributor(contributorVersion, gitRepositoryVersion.getCommits());
			for (File fileContributor : filesContributor) {
				for (FileVersion fileVersion : filesVersion) {
					if(fileVersion.getFile().isFile(fileContributor.getPath())) {
						AuthorFileExpertise authorFile = getAuthorFileByKnowledgeMetric(form.getKnowledgeMetric(), gitRepositoryVersion.getCommits(), contributorVersion, fileVersion);
						addToFileTotalKnowledge(form.getKnowledgeMetric(), fileVersion, authorFile);
						authorFiles.add(authorFile);
						break;
					}
				}
			}
		}
		roundTotalKnowledgeFilesVersion(filesVersion);
		gitRepositoryVersionKnowledgeModel.setFiles(filesVersion);
		setContributorTruckFactorData(contributorsVersion, authorFiles, gitRepositoryVersion.getFiles(), form.getKnowledgeMetric(), gitRepositoryVersion.getNumberAnalysedFiles());
		gitRepositoryVersionKnowledgeModel.setContributors(contributorsVersion);
		gitRepositoryVersionKnowledgeModel.setAuthorsFiles(authorFiles);
		gitRepositoryVersionKnowledgeModelRepository.save(gitRepositoryVersionKnowledgeModel);
		return gitRepositoryVersionKnowledgeModel;
	}

	private void roundTotalKnowledgeFilesVersion(List<FileVersion> filesVersion) {
		for(FileVersion version: filesVersion) {
			if(version.getTotalKnowledge() > 0) {
				BigDecimal bd = BigDecimal.valueOf(version.getTotalKnowledge());
				BigDecimal rounded = bd.setScale(4, RoundingMode.HALF_UP);
				version.setTotalKnowledge(rounded.doubleValue());
			}
		}
	}

	private void addFileToFilesAuthor(List<ContributorVersion> contributorsVersion, AuthorFileExpertise authorFile, File file) {
		for (ContributorVersion contributorKnowledgeModel: contributorsVersion) {
			if (authorFile.getContributorVersion().getContributor().equals(contributorKnowledgeModel.getContributor())) {
				contributorKnowledgeModel.getFilesAuthor().add(file);
				contributorKnowledgeModel.setNumberFilesAuthor(contributorKnowledgeModel.getNumberFilesAuthor()+1);
				break;
			}
		}
	}

	protected void setContributorTruckFactorData(List<ContributorVersion> contributorsVersion, 
			List<AuthorFileExpertise> authorsFiles, List<File> files, KnowledgeModel knowledgeMetric, int numberAnalysedFiles) {
		if (knowledgeMetric.equals(KnowledgeModel.DOE) || knowledgeMetric.equals(KnowledgeModel.DOA)) {
			for (File file: files) {
				List<AuthorFileExpertise> authorsFilesAux = authorsFiles.stream().
						filter(authorFile -> authorFile.getFileVersion().getFile().getPath().equals(file.getPath())).toList();
				if(authorsFilesAux != null && !authorsFilesAux.isEmpty()) {
					double maxValue = 0.0;
					for (AuthorFileExpertise authorFile : authorsFilesAux) {
						if(knowledgeMetric.equals(KnowledgeModel.DOE) && authorFile.getDoe().getDoeValue() > maxValue) {
							maxValue = authorFile.getDoe().getDoeValue();
						}else if(knowledgeMetric.equals(KnowledgeModel.DOA) && authorFile.getDoa().getDoaValue() > maxValue) {
							maxValue = authorFile.getDoa().getDoaValue();
						}
					}
					for (AuthorFileExpertise authorFile : authorsFilesAux) {
						double normalized = 0.0;
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
			}
			for (File file: files) {
				for (MlOutput mlOutput : output) {
					if(file.isFile(mlOutput.getFile()) && mlOutput.getDecision().equals("MANTENEDOR")) {
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
				contributorVersion.setPercentOfFilesAuthored(Double.valueOf((double)contributorVersion.getNumberFilesAuthor()/numberAnalysedFiles));
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

	private List<File> filesTouchedByContributor(ContributorVersion contributorVersion, List<Commit> commits){
		List<File> files = new ArrayList<>();
		List<Contributor> contributors = getContributorAndAliases(contributorVersion.getContributor());
		forCommit:for (Commit commit : commits) {
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					for (CommitFile commitFile: commit.getCommitFiles()) {
						if(files.stream().anyMatch(f -> f.isFile(commitFile.getFile().getPath()))) {
							continue;
						}
						files.add(commitFile.getFile());
					}
					continue forCommit;
				}
			}
		}
		return files;
	}

	private AuthorFileExpertise getAuthorFileByKnowledgeMetric(KnowledgeModel knowledgeMetric, List<Commit> commits, 
			ContributorVersion contributorVersion, FileVersion fileVersion) {
		if (knowledgeMetric.equals(KnowledgeModel.DOE) 
				|| knowledgeMetric.equals(KnowledgeModel.MACHINE_LEARNING)) {
			DOE doe = getDoeContributorFile(contributorVersion, fileVersion, commits);
			return new AuthorFileExpertise(contributorVersion, fileVersion, doe);
		}else {
			DOA doa = getDoaContributorFile(contributorVersion, fileVersion, commits);
			return new AuthorFileExpertise(contributorVersion, fileVersion, doa);
		}
	}

	private List<Contributor> getContributorAndAliases(Contributor contributor){
		List<Contributor> contributors = new ArrayList<>();
		contributors.add(contributor);
		if(contributor.getAlias() != null) {
			contributors.addAll(contributor.getAlias());
		}
		return contributors;
	}

	private int isContributorFa(List<Contributor> contributors, File file, List<Commit> commits) {
		for (Commit commit : commits) {
			for (CommitFile commitFile: commit.getCommitFiles()) {
				if(file.isFile(commitFile.getFile().getPath()) && 
						commitFile.getStatus().equals(OperationType.ADDED)) {
					for (Contributor contributorAux : contributors) {
						if(contributorAux.equals(commit.getAuthor())) {
							return 1;
						}
					}
					return 0;
				}
			}
		}
		return 0;
	}

	protected DOE getDoeContributorFile(ContributorVersion contributorVersion, 
			FileVersion fileVersion, List<Commit> commits) {
		List<Contributor> contributors = getContributorAndAliases(contributorVersion.getContributor());
		Date currentDate = new Date();
		int adds = 0; 
		int numDays =0;
		int fa = isContributorFa(contributors, fileVersion.getFile(), commits);
		Date dateLastCommit = commits.get(0).getAuthorDate();
		forCommit: for (Commit commit : commits) {
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					for (CommitFile commitFile: commit.getCommitFiles()) {
						if (fileVersion.getFile().isFile(commitFile.getFile().getPath())) {
							adds = commitFile.getAdditions() + adds;
							if (commit.getAuthorDate().after(dateLastCommit)) {
								dateLastCommit = commit.getAuthorDate();
							}
							continue forCommit;
						}
					}
				}
			}
		}
		if (dateLastCommit != null) {
			long diff = currentDate.getTime() - dateLastCommit.getTime();
			numDays = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
		}
		double doeValue = doeUtils.getDOE(adds, fa,
				numDays, fileVersion.getFile().getSize());
		return new DOE(adds, fa, numDays, fileVersion.getFile().getSize(), doeValue);
	}

	private DOA getDoaContributorFile(ContributorVersion contributorVersion, 
			FileVersion fileVersion, List<Commit> commits) {
		List<Contributor> contributors = getContributorAndAliases(contributorVersion.getContributor());
		int dl = 0;
		int ac = 0; 
		int fa = isContributorFa(contributors, fileVersion.getFile(), commits);
		for (Commit commit : commits) {
			boolean present = false;
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					present = true;
					break;
				}
			}
			if (present) {
				for (CommitFile commitFile: commit.getCommitFiles()) {
					if (commitFile.getFile().getPath().equals(fileVersion.getFile().getPath())) {
						dl = dl + 1;
						break;
					}
				}
			}else {
				for (CommitFile commitFile: commit.getCommitFiles()) {
					if (commitFile.getFile().getPath().equals(fileVersion.getFile().getPath())) {
						ac = ac + 1;
						break;
					}
				}
			}
		}
		double doaValue = doaUtils.getDOA(fa, dl,ac);
		return new DOA(fa, dl, ac, doaValue);
	}

	public List<GitRepositoryVersionKnowledgeModel> getByGitRepositoryVersionId(Long id) {
		return gitRepositoryVersionKnowledgeModelRepository.findByRepositoryVersionId(id);
	}

	public void saveRepositoryVersionKnowledgeSharedLinks() {
		List<SharedLinkCommit> sharedLinkCommits = sharedLinkCommitRepository.findByCommitFileAddedLinkIsNotNull();
		Set<GitRepository> repositories = new HashSet<>();
		sharedLinkCommits.forEach(slc -> repositories.add(slc.getFileRepositorySharedLinkCommit().getGitRepository()));
		for (GitRepository gitRepository : repositories) {
			List<GitRepositoryVersion> versions = gitRepositoryVersionRepository.findByGitRepositoryId(gitRepository.getId());
			for (GitRepositoryVersion version : versions) {
				try {
					saveGitRepositoryVersionKnowledgeModel(GitRepositoryVersionKnowledgeModelForm1.builder()
							.idGitRepositoryVersion(version.getId()).knowledgeMetric(KnowledgeModel.DOE).foldersPaths(null).build());
				}catch (Exception e) {
					log.info(e.getMessage());
				}
			}
		}
	}

}
