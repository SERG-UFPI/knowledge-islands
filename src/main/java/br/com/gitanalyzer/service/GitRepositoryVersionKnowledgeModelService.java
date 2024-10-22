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
import java.util.stream.Collectors;

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
import br.com.gitanalyzer.model.entity.GitRepositoryFolder;
import br.com.gitanalyzer.model.entity.GitRepositoryVersion;
import br.com.gitanalyzer.model.entity.GitRepositoryVersionKnowledgeModel;
import br.com.gitanalyzer.model.enums.KnowledgeModel;
import br.com.gitanalyzer.model.enums.OperationType;
import br.com.gitanalyzer.model.vo.MlOutput;
import br.com.gitanalyzer.repository.GitRepositoryFolderRepository;
import br.com.gitanalyzer.repository.GitRepositoryVersionKnowledgeModelRepository;
import br.com.gitanalyzer.repository.GitRepositoryVersionRepository;
import br.com.gitanalyzer.utils.DoaUtils;
import br.com.gitanalyzer.utils.DoeUtils;
import br.com.gitanalyzer.utils.KnowledgeIslandsUtils;

@Service
public class GitRepositoryVersionKnowledgeModelService {

	@Autowired
	private GitRepositoryVersionKnowledgeModelRepository gitRepositoryVersionKnowledgeModelRepository;
	@Autowired
	private GitRepositoryVersionRepository gitRepositoryVersionRepository;
	@Autowired
	private GitRepositoryFolderRepository gitRepositoryFolderRepository;
	private DoeUtils doeUtils = new DoeUtils();
	private DoaUtils doaUtils = new DoaUtils();
	private String[] header = new String[] {"Adds", "QuantDias", "TotalLinhas", "PrimeiroAutor", "Author", "File"};

	public GitRepositoryVersionKnowledgeModelForm1 convertModelForm1ModelForm2(GitRepositoryVersionKnowledgeModelForm2 form) {
		GitRepositoryVersionKnowledgeModelForm1 form1  = new GitRepositoryVersionKnowledgeModelForm1();
		form1.setIdGitRepositoryVersion(form.getIdGitRepositoryVersion());
		form1.setKnowledgeMetric(form.getKnowledgeMetric());
		if(form.getFoldersIds() != null && form.getFoldersIds().size() > 0) {
			List<GitRepositoryFolder> folders = gitRepositoryFolderRepository.findAllById(form.getFoldersIds());
			form1.setFoldersPaths(folders.stream().map(f -> f.getPath()).filter(f -> f!=null).toList());
		}
		return form1;
	}

	@Transactional
	public GitRepositoryVersionKnowledgeModel saveGitRepositoryVersionKnowledgeModel(GitRepositoryVersionKnowledgeModelForm1 form) throws Exception {
		GitRepositoryVersion gitRepositoryVersion = gitRepositoryVersionRepository.findById(form.getIdGitRepositoryVersion()).get();
		Collections.sort(gitRepositoryVersion.getCommits(), Collections.reverseOrder());
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
		List<ContributorVersion> contributorsVersion = new ArrayList<>();
		gitRepositoryVersion.getContributors().stream().forEach(c -> contributorsVersion.add(new ContributorVersion(c)));
		for(ContributorVersion contributorVersion: contributorsVersion) {
			List<File> filesContributor = filesTouchedByContributor(contributorVersion, gitRepositoryVersion.getCommits());
			forFileContributor: for (File fileContributor : filesContributor) {
				for (FileVersion fileVersion : filesVersion) {
					if(fileVersion.getFile().isFile(fileContributor.getPath())) {
						AuthorFileExpertise authorFile = getAuthorFileByKnowledgeMetric(form.getKnowledgeMetric(), gitRepositoryVersion.getCommits(), contributorVersion, fileVersion);
						addFileKnowledgeOfAuthor(form.getKnowledgeMetric(), fileVersion, authorFile);
						authorFiles.add(authorFile);
						continue forFileContributor;
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
				BigDecimal rounded = bd.setScale(2, RoundingMode.HALF_UP);
				version.setTotalKnowledge(rounded.doubleValue());
			}
		}
	}

	protected void setContributorTruckFactorData(List<ContributorVersion> contributorsVersion, 
			List<AuthorFileExpertise> authorsFiles, List<File> files, KnowledgeModel knowledgeMetric, int numberAnalysedFiles) {
		if (knowledgeMetric.equals(KnowledgeModel.DOE) || knowledgeMetric.equals(KnowledgeModel.DOA)) {
			for (File file: files) {
				List<AuthorFileExpertise> authorsFilesAux = authorsFiles.stream().
						filter(authorFile -> authorFile.getFileVersion().getFile().getPath().equals(file.getPath())).collect(Collectors.toList());
				if(authorsFilesAux != null && authorsFilesAux.size() > 0) {
					double maxValue = 0.0;
					for (AuthorFileExpertise authorFile : authorsFilesAux) {
						if(knowledgeMetric.equals(KnowledgeModel.DOE) && authorFile.getDoe().getDoeValue() > maxValue) {
							maxValue = authorFile.getDoe().getDoeValue();
						}
						if(knowledgeMetric.equals(KnowledgeModel.DOA) && authorFile.getDoa().getDoaValue() > maxValue) {
							maxValue = authorFile.getDoa().getDoaValue();
						}
					}
					for (AuthorFileExpertise authorFile : authorsFilesAux) {
						double normalized = 0.0;
						boolean maintainer = false;
						if (knowledgeMetric.equals(KnowledgeModel.DOE)) {
							normalized = authorFile.getDoe().getDoeValue()/maxValue;
							if (normalized >= KnowledgeIslandsUtils.normalizedThresholdMantainerDOE) {
								maintainer = true;
							}
						}else if(knowledgeMetric.equals(KnowledgeModel.DOA)){
							normalized = authorFile.getDoa().getDoaValue()/maxValue;
							if (normalized > KnowledgeIslandsUtils.normalizedThresholdMantainerDOA && 
									authorFile.getDoa().getDoaValue() >= KnowledgeIslandsUtils.thresholdMantainerDOA) {
								maintainer = true;
							}
						}
						if(maintainer == true) {
							for (ContributorVersion contributorKnowledgeModel: contributorsVersion) {
								if (authorFile.getAuthorVersion().getContributor().equals(contributorKnowledgeModel.getContributor())) {
									contributorKnowledgeModel.getFilesAuthor().add(file);
									contributorKnowledgeModel.setNumberFilesAuthor(contributorKnowledgeModel.getNumberFilesAuthor()+1);
									break;
								}
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
							authorFile.getAuthorVersion().getContributor().getEmail(),
							authorFile.getFileVersion().getFile().getPath()
					});
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			List<MlOutput> output = new ArrayList<MlOutput>();
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

	private void addFileKnowledgeOfAuthor(KnowledgeModel knowledgeMetric, FileVersion fileVersion, AuthorFileExpertise authorFile) {
		if (knowledgeMetric.equals(KnowledgeModel.DOE) 
				|| knowledgeMetric.equals(KnowledgeModel.MACHINE_LEARNING)) {
			fileVersion.setTotalKnowledge(fileVersion.getTotalKnowledge()+authorFile.getDoe().getDoeValue());
		}else {
			fileVersion.setTotalKnowledge(fileVersion.getTotalKnowledge()+authorFile.getDoa().getDoaValue());
		}
	}

	private List<File> filesTouchedByContributor(ContributorVersion contributorVersion, List<Commit> commits){
		List<File> files = new ArrayList<File>();
		List<Contributor> contributors = new ArrayList<Contributor>();
		contributors.add(contributorVersion.getContributor());
		if(contributorVersion.getContributor().getAlias() != null) {
			contributors.addAll(contributorVersion.getContributor().getAlias());
		}
		forCommit:for (Commit commit : commits) {
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					forCommitFile: for (CommitFile commitFile: commit.getCommitFiles()) {
						for (File file : files) {
							if (file.isFile(commitFile.getFile().getPath())) {
								continue forCommitFile;
							}
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
			double doeValue = doeUtils.getDOE(doe.getAdds(), doe.getFa(),
					doe.getNumDays(), fileVersion.getFile().getSize());
			doe.setDoeValue(doeValue);
			doe.setSize(fileVersion.getFile().getSize());
			return new AuthorFileExpertise(contributorVersion, fileVersion, doe);
		}else {
			DOA doa = getDoaContributorFile(contributorVersion, fileVersion, commits);
			double doaValue = doaUtils.getDOA(doa.getFa(), doa.getDl(),
					doa.getAc());
			doa.setDoaValue(doaValue);
			return new AuthorFileExpertise(contributorVersion, fileVersion, doa);
		}
	}

	protected DOE getDoeContributorFile(ContributorVersion contributorVersion, 
			FileVersion fileVersion, List<Commit> commits) {
		Date currentDate = commits.get(0).getAuthorDate();
		int adds = 0; 
		int fa = 0;
		int numDays =0;;
		Date dateLastCommit = null;
		List<Contributor> contributors = new ArrayList<Contributor>();
		contributors.add(contributorVersion.getContributor());
		if(contributorVersion.getContributor().getAlias() != null) {
			contributors.addAll(contributorVersion.getContributor().getAlias());
		}
		forCommit: for (Commit commit : commits) {
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					for (CommitFile commitFile: commit.getCommitFiles()) {
						if (fileVersion.getFile().isFile(commitFile.getFile().getPath())) {
							adds = commitFile.getAdditions() + adds;
							if (dateLastCommit == null) {
								dateLastCommit = commit.getAuthorDate();
							}
							if(commitFile.getStatus().equals(OperationType.ADDED)) {
								fa = 1;
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
		return new DOE(adds, fa, numDays);
	}

	protected DOA getDoaContributorFile(ContributorVersion contributorVersion, 
			FileVersion fileVersion, List<Commit> commits) {
		int dl = 0, ac = 0, fa = 0;
		List<Contributor> contributors = new ArrayList<Contributor>();
		contributors.add(contributorVersion.getContributor());
		if(contributorVersion.getContributor().getAlias() != null) {
			contributors.addAll(contributorVersion.getContributor().getAlias());
		}
		for (Commit commit : commits) {
			boolean present = false;
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					present = true;
					break;
				}
			}
			if (present == true) {
				for (CommitFile commitFile: commit.getCommitFiles()) {
					if (commitFile.getFile().getPath().equals(fileVersion.getFile().getPath())) {
						dl = dl + 1;
						if(commitFile.getStatus().equals(OperationType.ADDED)) {
							fa = 1;
						}
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
		return new DOA(fa, dl, ac);
	}

	public List<GitRepositoryVersionKnowledgeModel> getByGitRepositoryVersionId(Long id) {
		return gitRepositoryVersionKnowledgeModelRepository.findByRepositoryVersionId(id);
	}

}
