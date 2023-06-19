package br.com.gitanalyzer.service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import br.com.gitanalyzer.dto.TruckFactorDTO;
import br.com.gitanalyzer.dto.form.HistoryReposTruckFactorForm;
import br.com.gitanalyzer.enums.KnowledgeMetric;
import br.com.gitanalyzer.enums.OperationType;
import br.com.gitanalyzer.enums.StageEnum;
import br.com.gitanalyzer.extractors.CommitExtractor;
import br.com.gitanalyzer.extractors.HistoryCommitsExtractor;
import br.com.gitanalyzer.extractors.ProjectVersionExtractor;
import br.com.gitanalyzer.main.dto.CloneRepoForm;
import br.com.gitanalyzer.main.dto.RepositoryKnowledgeMetricDTO;
import br.com.gitanalyzer.main.vo.CommitFiles;
import br.com.gitanalyzer.main.vo.MlOutput;
import br.com.gitanalyzer.model.AuthorFile;
import br.com.gitanalyzer.model.Commit;
import br.com.gitanalyzer.model.CommitFile;
import br.com.gitanalyzer.model.File;
import br.com.gitanalyzer.model.MetricsDoa;
import br.com.gitanalyzer.model.MetricsDoe;
import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.Project;
import br.com.gitanalyzer.model.entity.ProjectVersion;
import br.com.gitanalyzer.model.entity.TruckFactor;
import br.com.gitanalyzer.model.entity.TruckFactorDevelopers;
import br.com.gitanalyzer.model.entity.TruckFactorProcess;
import br.com.gitanalyzer.repository.ProjectRepository;
import br.com.gitanalyzer.repository.ProjectVersionRepository;
import br.com.gitanalyzer.repository.TruckFactorDevelopersRepository;
import br.com.gitanalyzer.repository.TruckFactorProcessRepository;
import br.com.gitanalyzer.repository.TruckFactorRepository;
import br.com.gitanalyzer.utils.Constants;
import br.com.gitanalyzer.utils.DoaUtils;
import br.com.gitanalyzer.utils.DoeUtils;
import br.com.gitanalyzer.utils.ProjectUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TruckFactorService {

	private DoeUtils doeUtils = new DoeUtils();
	private DoaUtils doaUtils = new DoaUtils();
	private ProjectUtils projectUtils = new ProjectUtils();
	private ProjectVersionExtractor projectVersionExtractor = new ProjectVersionExtractor();
	private CommitExtractor commitExtractor = new CommitExtractor();
	private String[] header = new String[] {"Adds", "QuantDias", "TotalLinhas", "PrimeiroAutor", "Author", "File"};

	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private TruckFactorRepository truckFactorRepository;
	@Autowired
	private TruckFactorDevelopersRepository truckFactorDevelopersRepository;
	@Autowired
	private ProjectVersionRepository projectVersionRepository;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private DownloaderService downloaderService;
	@Autowired
	private TruckFactorProcessRepository truckFactorProcessRepository; 


	private static List<String> invalidsProjects = new ArrayList<String>(Arrays.asList("sass", 
			"ionic", "cucumber"));

	class CloneTruckFactorTask implements Runnable {

		private CloneRepoForm form;
		private TruckFactorProcess process;

		CloneTruckFactorTask(CloneRepoForm form, TruckFactorProcess process){
			this.form = form;
			this.process = process;
		}

		@Override
		public void run() {
			try {
				String projectPath = downloaderService.cloneProject(form);
				setProcessStage(process, StageEnum.EXTRACT_DATA);
				projectService.generateLogFiles(projectPath);
				setProcessStage(process, StageEnum.CALCULATING);
				TruckFactorDTO truckFactor = generateTruckFactorProject(RepositoryKnowledgeMetricDTO.builder()
						.path(projectPath).knowledgeMetric(KnowledgeMetric.DOE).build());
				process.setTruckFactor(truckFactorRepository.findById(truckFactor.getId()).get());
				process.setEndDate(new Date());
				setProcessStage(process, StageEnum.ANALYSIS_FINISHED);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}

	}

	public void setProcessStage(TruckFactorProcess process, StageEnum stage) {
		process.setStage(stage);
		truckFactorProcessRepository.save(process);
	}

	public TruckFactorProcess cloneAndCalculateTruckFactor(CloneRepoForm form) {
		TruckFactorProcess process = new TruckFactorProcess(StageEnum.DOWNLOAD);
		truckFactorProcessRepository.save(process);
		Thread t = new Thread(new CloneTruckFactorTask(form, process));
		t.start();
		return process;
	}

	private List<Commit> getCommitsFromAHash(List<Commit> commits, String hash){
		boolean flag = false;
		List<Commit> commitsReturn = new ArrayList<Commit>();
		for(int i = 0; i < commits.size(); i++){
			if(commits.get(i).getExternalId().equals(hash)) {
				flag = true; 
			}
			if(flag == true) {
				commitsReturn.add(commits.get(i));
			}
		}
		return commitsReturn;
	}

	public TruckFactorDTO generateTruckFactorProject(RepositoryKnowledgeMetricDTO repo)
			throws IOException, NoHeadException, GitAPIException {
		String projectPath = repo.getPath();
		KnowledgeMetric knowledgeMetric = repo.getKnowledgeMetric();
		String projectName = projectUtils.extractProjectName(projectPath);
		Project project = null;
		ProjectVersion projectVersion = null;
		TruckFactor truckFactor = null;
		if(projectRepository.existsByName(projectName)) {
			project = projectRepository.findByName(projectName);
		}else {
			project = new Project(projectName, repo.getPath());
		}
		//if (projectName.equals("rails") == true) {
		filteringProjectsCommentsStudy(project);
		boolean versionAnalyzed = false;
		if(project.getId() != null) {
			String lastCommitHash = commitExtractor.getLastCommitHash(projectPath);
			versionAnalyzed = project.getVersions().stream().anyMatch(v -> v.getVersionId().equals(lastCommitHash));
		}
		if(project.isFiltered() == false && versionAnalyzed == false) {
			log.info("EXTRACTING DATA FROM "+projectPath);
			projectVersion = projectVersionExtractor.extractProjectVersion(projectPath, projectName);
			projectService.createFolderLogsAndCopyFiles(projectPath, projectName, projectVersion.getVersionId());
			log.info("CALCULATING "+knowledgeMetric.getName()+" OF "+projectName);
			List<AuthorFile> authorFiles = new ArrayList<AuthorFile>();
			for(Contributor contributor: projectVersion.getContributors()) {
				List<File> filesContributor = filesTouchedByContributor(contributor, projectVersion.getCommits());
				for (File file : projectVersion.getFiles()) {
					for (File fileContributor : filesContributor) {
						if(fileContributor.getPath().equals(file.getPath())) {
							if (knowledgeMetric.equals(KnowledgeMetric.DOE) || knowledgeMetric.equals(KnowledgeMetric.MACHINE_LEARNING)) {
								DoeContributorFile doeContributorFile = getDoeContributorFile(contributor, file, projectVersion.getCommits());
								double doe = doeUtils.getDOE(doeContributorFile.numberAdds, doeContributorFile.fa,
										doeContributorFile.numDays, file.getFileSize());
								MetricsDoe metricsDoe = new MetricsDoe(doeContributorFile.numberAdds, doeContributorFile.fa,
										doeContributorFile.numDays, file.getFileSize());
								authorFiles.add(new AuthorFile(contributor, file, doe, metricsDoe));
							}else {
								DoaContributorFile doaContributorFile = getDoaContributorFile(contributor, file, projectVersion.getCommits());
								double doa = doaUtils.getDOA(doaContributorFile.fa, doaContributorFile.numberCommits,
										doaContributorFile.ac);
								MetricsDoa metricsDoa = new MetricsDoa(doaContributorFile.fa, doaContributorFile.numberCommits, doaContributorFile.ac);
								authorFiles.add(new AuthorFile(contributor, doa, file, metricsDoa));
							}
							break;
						}
					}
				}
			}
			setContributorNumberAuthorAndFileMaintainers(projectVersion.getContributors(), authorFiles, projectVersion.getFiles(), knowledgeMetric);
			projectVersion.getContributors().removeIf(contributor -> contributor.getNumberFilesAuthor() == 0);
			projectVersion.setNumberAuthors(projectVersion.getContributors().size());
			Collections.sort(projectVersion.getContributors(), new Comparator<Contributor>() {
				@Override
				public int compare(Contributor c1, Contributor c2) {
					return Integer.compare(c2.getNumberFilesAuthor(), c1.getNumberFilesAuthor());
				}
			});
			List<Contributor> topContributors = new ArrayList<Contributor>();
			log.info("CALCULATING TF OF "+projectName);
			int tf = 0;
			List<File> coveredFiles = null;
			while(projectVersion.getContributors().isEmpty() == false) {
				double fileSize = projectVersion.getFiles().size();
				coveredFiles = getCoverageFiles(projectVersion.getContributors(), projectVersion.getFiles(), knowledgeMetric);
				double numberFilesCovarage = coveredFiles.size(); 
				double coverage = numberFilesCovarage/fileSize;
				if(coverage < 0.5) 
					break;
				topContributors.add(projectVersion.getContributors().get(0));
				projectVersion.getContributors().remove(0);
				tf = tf+1;
			}
			log.info("SAVING TF DATA OF "+projectName);
			if(project.getId() == null) {
				projectRepository.save(project);
			}
			if(projectVersionRepository.existsByVersionId(projectVersion.getVersionId()) == false) {
				projectVersion.setProject(project);
				projectVersionRepository.save(projectVersion);
				truckFactor = new TruckFactor(tf, projectVersion, knowledgeMetric, getImplicatedFiles(coveredFiles, projectVersion.getFiles()));
				//TruckFactor truckFactor = new TruckFactor(tf, projectVersion, knowledgeMetric);
				truckFactorRepository.save(truckFactor);
				for (Contributor contributor : topContributors) {
					TruckFactorDevelopers truckFactorDevelopers = new TruckFactorDevelopers(contributor.getName(), contributor.getEmail(),
							truckFactor, (double)contributor.getNumberFilesAuthor()/(double)projectVersion.getNumberAnalysedFiles());
					if (truckFactorDevelopersRepository.
							existsByTruckFactorIdAndNameAndEmail(truckFactor.getId(), contributor.getName(),
									contributor.getEmail()) == false) {
						truckFactorDevelopersRepository.save(truckFactorDevelopers);
						if(truckFactor.getTruckFactorDevelopers() == null) {
							truckFactor.setTruckFactorDevelopers(new ArrayList<>());
						}
						truckFactor.getTruckFactorDevelopers().add(truckFactorDevelopers);
					}
				}
			}
		}
		return truckFactor.toDto();
	}

	private List<String> getImplicatedFiles(List<File> coveredFiles, List<File> files) {
		List<String> filePaths = new ArrayList<String>();
		forFile:for (File file : files) {
			for(File coveredFile: coveredFiles) {
				if (file.isFile(coveredFile.getPath())) {
					continue forFile;
				}
			}
			filePaths.add(file.getPath());
		} 
		return filePaths;
	}

	private void filteringProjectsCommentsStudy(Project project) {
		if(invalidsProjects.contains(project.getName())) {
			project.setFiltered(true);
		}
	}

	private void commitsFilesFrequency(List<Commit> commits, List<File> files) {
		for(Commit commit: commits) {
			for(CommitFile commitFile: commit.getCommitFiles()) {
				for (File file : files) {
					if(file.getPath().equals(commitFile.getFile().getPath())) {
						file.setNumberCommits(file.getNumberCommits()+1);
						break;
					}
				}
			}
		}
		files = files.stream().sorted().collect(Collectors.toList());
		try {
			FileWriter fw = new FileWriter(Constants.pathCommitFilesFrequencyLog);
			CSVWriter writer = new CSVWriter(fw);
			for (File file : files) {
				writer.writeNext(file.toStringArray());
			}
			writer.close();
		}catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void saveNumberFilesOfCommits(List<Commit> commits) {
		try {
			FileWriter fw = new FileWriter(Constants.pathCommitFilesLog);
			CSVWriter writer = new CSVWriter(fw);
			List<CommitFiles> commitsFiles = new ArrayList<CommitFiles>();
			for (Commit commit : commits) {
				commitsFiles.add(new CommitFiles(commit.getExternalId(), commit.getCommitFiles().size()));
			}
			commitsFiles = commitsFiles.stream().filter(c -> c.getNumberOfFilesModified() != 0).collect(Collectors.toList());
			commitsFiles = commitsFiles.stream().sorted().collect(Collectors.toList());
			for (CommitFiles commitFiles : commitsFiles) {
				writer.writeNext(commitFiles.toStringArray());
			}
			writer.close();
		}
		catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	protected DoeContributorFile getDoeContributorFile(Contributor contributor, 
			File file, List<Commit> commits) {
		Date currentDate = commits.get(0).getDate();
		int adds = 0;
		int fa = 0;
		int numDays = 0;
		Date dateLastCommit = null;
		List<Contributor> contributors = new ArrayList<Contributor>();
		contributors.add(contributor);
		contributors.addAll(contributor.getAlias());
		for (Commit commit : commits) {
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					for (CommitFile commitFile: commit.getCommitFiles()) {
						if (commitFile.getFile().getPath().equals(file.getPath())) {
							adds = commitFile.getAdds() + adds;
							if (dateLastCommit == null) {
								dateLastCommit = commit.getDate();
							}
							if(commitFile.getOperation().equals(OperationType.ADD)) {
								fa = 1;
							}
							break;
						}
					}
					break;
				}
			}
		}
		if (dateLastCommit != null) {
			long diff = currentDate.getTime() - dateLastCommit.getTime();
			numDays = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
		}
		DoeContributorFile doeContributorFile = new DoeContributorFile(adds, fa, numDays);
		return doeContributorFile;
	}

	protected DoaContributorFile getDoaContributorFile(Contributor contributor, 
			File file, List<Commit> commits) {
		int numberCommits = 0, ac = 0;
		int fa = 0;
		List<Contributor> contributors = new ArrayList<Contributor>();
		contributors.add(contributor);
		contributors.addAll(contributor.getAlias());
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
					if (commitFile.getFile().getPath().equals(file.getPath())) {
						numberCommits = numberCommits + 1;
						if(commitFile.getOperation().equals(OperationType.ADD)) {
							fa = 1;
						}
						break;
					}
				}
			}else {
				for (CommitFile commitFile: commit.getCommitFiles()) {
					if (commitFile.getFile().getPath().equals(file.getPath())) {
						ac = ac + 1;
						break;
					}
				}
			}
		}
		DoaContributorFile doaContributorFile = new DoaContributorFile(numberCommits, fa, ac);
		return doaContributorFile;
	}

	private List<File> filesTouchedByContributor(Contributor contributor, List<Commit> commits){
		List<File> files = new ArrayList<File>();
		List<Contributor> contributors = new ArrayList<Contributor>();
		contributors.add(contributor);
		contributors.addAll(contributor.getAlias());
		for (Commit commit : commits) {
			for (Contributor contributorAux : contributors) {
				if (contributorAux.equals(commit.getAuthor())) {
					for (CommitFile commitFile: commit.getCommitFiles()) {
						boolean filePresent = false;
						for (File file : files) {
							if (file.getPath().equals(commitFile.getFile().getPath())) {
								filePresent = true;
								break;
							}
						}
						if (filePresent == false) {
							files.add(commitFile.getFile());
						}
					}
					break;
				}
			}
		}
		return files;
	}

	public void directoriesTruckFactorAnalyzes(RepositoryKnowledgeMetricDTO request) throws IOException, 
	NoHeadException, GitAPIException{
		java.io.File dir = new java.io.File(request.getPath());
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				RepositoryKnowledgeMetricDTO repo = new RepositoryKnowledgeMetricDTO(projectPath, request.getKnowledgeMetric());
				generateTruckFactorProject(repo);
			}
		}
	}

	protected List<File> getCoverageFiles(List<Contributor> contributors, List<File> files, KnowledgeMetric knowledgeMetric) {
		List<File> coveredFiles = new ArrayList<File>();
		forFiles:for(File file: files) {
			if (file.getMantainers().size() > 0) {
				for (Contributor maintainer : file.getMantainers()) {
					for (Contributor contributor : contributors) {
						if (maintainer.equals(contributor)) {
							coveredFiles.add(file);
							continue forFiles;
						}
					}
				}
			}
		}
		return coveredFiles;
	}

	protected void setContributorNumberAuthorAndFileMaintainers(List<Contributor> contributors, 
			List<AuthorFile> authorsFiles, List<File> files, KnowledgeMetric knowledgeMetric) {
		if (knowledgeMetric.equals(KnowledgeMetric.MACHINE_LEARNING) == false) {
			for (File file: files) {
				List<AuthorFile> authorsFilesAux = authorsFiles.stream().
						filter(authorFile -> authorFile.getFile().getPath().equals(file.getPath())).collect(Collectors.toList());
				if(authorsFilesAux != null && authorsFilesAux.size() > 0) {
					AuthorFile max = authorsFilesAux.stream().max(
							Comparator.comparing(knowledgeMetric.equals(KnowledgeMetric.DOE)?AuthorFile::getDoe:AuthorFile::getDoa)).get();
					for (AuthorFile authorFile : authorsFilesAux) {
						double normalized = 0;
						boolean maintainer = false;
						if (knowledgeMetric.equals(KnowledgeMetric.DOE)) {
							normalized = authorFile.getDoe()/max.getDoe();
							if (normalized >= Constants.normalizedThresholdMantainerDOE) {
								maintainer = true;
							}
						}else if(knowledgeMetric.equals(KnowledgeMetric.DOA)){
							normalized = authorFile.getDoa()/max.getDoa();
							if (normalized > Constants.normalizedThresholdMantainerDOA && 
									authorFile.getDoa() >= Constants.thresholdMantainerDOA) {
								maintainer = true;
							}
						}
						if(maintainer == true) {
							file.getMantainers().add(authorFile.getAuthor());
							for (Contributor contributor: contributors) {
								if (authorFile.getAuthor().equals(contributor)) {
									contributor.setNumberFilesAuthor(contributor.getNumberFilesAuthor()+1);
									break;
								}
							}
						}
					}
				}
			}
		}else {
			java.io.File fileInput = new java.io.File(Constants.pathInputMlFile);
			FileWriter outputfile;
			try {
				outputfile = new FileWriter(fileInput);
				CSVWriter writer = new CSVWriter(outputfile);
				writer.writeNext(header);
				for (AuthorFile authorFile : authorsFiles) {
					writer.writeNext(new String[] {
							String.valueOf(authorFile.getMetricsDoe().getAdds()),
							String.valueOf(authorFile.getMetricsDoe().getNumDays()),
							String.valueOf(authorFile.getMetricsDoe().getSize()),
							String.valueOf(authorFile.getMetricsDoe().getFa()),
							authorFile.getAuthor().getEmail(),
							authorFile.getFile().getPath()
					});
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			List<MlOutput> output = new ArrayList<MlOutput>();
			try {
				ProcessBuilder pb = new ProcessBuilder("/usr/bin/Rscript", Constants.pathScriptMlFile);
				pb.redirectOutput(Redirect.INHERIT);
				pb.redirectError(Redirect.INHERIT);
				Process process = pb.start();
				process.waitFor();
				CSVReader reader = new CSVReader(new FileReader(Constants.pathOutputMlFile));
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
						for (Contributor contributor : contributors) {
							if (contributor.getEmail().equals(mlOutput.getAuthor())) {
								file.getMantainers().add(contributor);
								contributor.setNumberFilesAuthor(contributor.getNumberFilesAuthor()+1);
								break;
							}
						}
					}
				}
			}
		}
	}

	class DoeContributorFile{
		int numberAdds, fa, numDays;

		public DoeContributorFile(int numberAdds, int fa, int numDays) {
			super();
			this.numberAdds = numberAdds;
			this.fa = fa;
			this.numDays = numDays;
		}
	}

	class DoaContributorFile{
		int numberCommits, fa, ac;

		public DoaContributorFile(int numberCommits, int fa, int ac) {
			super();
			this.numberCommits = numberCommits;
			this.fa = fa;
			this.ac = ac;
		}
	}

	public void historyReposTruckFactor(HistoryReposTruckFactorForm form) throws URISyntaxException, IOException, InterruptedException, NoHeadException, GitAPIException {
		java.io.File dir = new java.io.File(form.getPath());
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				historyRepoTruckFactor(HistoryReposTruckFactorForm.builder().knowledgeMetric(KnowledgeMetric.DOE).numberYears(form.getNumberYears()).path(projectPath).build());
			}
		}
	}
	
	public void historyRepoTruckFactor(HistoryReposTruckFactorForm form) throws NoHeadException, IOException, GitAPIException, InterruptedException, URISyntaxException {
		String pathCheckoutScript = TruckFactorService.class.getResource("/checkout_script.sh").toURI().getPath();
		HistoryCommitsExtractor historyCommitsExtractor = new HistoryCommitsExtractor();
		Project project = projectService.returnProjectByPath(form.getPath());
		if(project.isFiltered() == false) {
			String[] hashes = historyCommitsExtractor.getCommitHashes(form.getPath(), form.getNumberYears());
			for (String hash : hashes) {
				String command = "sh "+pathCheckoutScript+" "+form.getPath()+" "+hash;
				Process process = Runtime.getRuntime().exec(command);
				process.waitFor();
				projectService.generateLogFiles(form.getPath());
				generateTruckFactorProject(RepositoryKnowledgeMetricDTO.builder()
						.knowledgeMetric(form.getKnowledgeMetric()).path(form.getPath()).build());
			}
			String command = "sh "+pathCheckoutScript+" "+form.getPath()+" "+hashes[0];
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
		}
	}
}
