package br.com.gitanalyzer.main;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import br.com.gitanalyzer.enums.KnowledgeMetric;
import br.com.gitanalyzer.enums.OperationType;
import br.com.gitanalyzer.extractors.ProjectVersionExtractor;
import br.com.gitanalyzer.main.dto.RepositoryKnowledgeMetricDTO;
import br.com.gitanalyzer.main.vo.CommitFiles;
import br.com.gitanalyzer.main.vo.MlOutput;
import br.com.gitanalyzer.model.AuthorFile;
import br.com.gitanalyzer.model.Commit;
import br.com.gitanalyzer.model.CommitFile;
import br.com.gitanalyzer.model.Contributor;
import br.com.gitanalyzer.model.File;
import br.com.gitanalyzer.model.MetricsDoa;
import br.com.gitanalyzer.model.MetricsDoe;
import br.com.gitanalyzer.model.Project;
import br.com.gitanalyzer.model.ProjectVersion;
import br.com.gitanalyzer.model.TruckFactor;
import br.com.gitanalyzer.model.TruckFactorDevelopers;
import br.com.gitanalyzer.repository.ProjectRepository;
import br.com.gitanalyzer.repository.ProjectVersionRepository;
import br.com.gitanalyzer.repository.TruckFactorDevelopersRepository;
import br.com.gitanalyzer.repository.TruckFactorRepository;
import br.com.gitanalyzer.utils.Constants;
import br.com.gitanalyzer.utils.DoaUtils;
import br.com.gitanalyzer.utils.DoeUtils;
import br.com.gitanalyzer.utils.ProjectUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TruckFactorAnalyzer {

	private DoeUtils doeUtils = new DoeUtils();
	private DoaUtils doaUtils = new DoaUtils();
	private ProjectUtils projectUtils = new ProjectUtils();
	private ProjectVersionExtractor projectVersionExtractor = new ProjectVersionExtractor();
	private String[] header = new String[] {"Adds", "QuantDias", "TotalLinhas", "PrimeiroAutor", "Author", "File"};

	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private TruckFactorRepository truckFactorRepository;
	@Autowired
	private TruckFactorDevelopersRepository truckFactorDevelopersRepository;
	@Autowired
	private ProjectVersionRepository projectVersionRepository;

	private static List<String> invalidsProjects = new ArrayList<String>(Arrays.asList("sass", 
			"ionic", "cucumber"));

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

	public void projectTruckFactorAnalyzes(RepositoryKnowledgeMetricDTO repo)
			throws IOException, NoHeadException, GitAPIException {
		String projectPath = repo.getPath();
		KnowledgeMetric knowledgeMetric = repo.getKnowledgeMetric();
		String projectName = projectUtils.extractProjectName(projectPath);
		Project project = null;
		if(projectRepository.existsByName(projectName)) {
			project = projectRepository.findByName(projectName);
		}else {
			project = new Project(projectName);
		}
		//if (projectName.equals("rails") == true) {
		filteringProjectsCommentsStudy(project);
		if(project.isFiltered() == false) {
			log.info("EXTRACTING DATA FROM "+projectPath);
			ProjectVersion projectVersion = projectVersionExtractor.extractProjectVersion(projectPath, projectName);

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
			while(projectVersion.getContributors().isEmpty() == false) {
				double covarage = getCoverage(projectVersion.getContributors(), projectVersion.getFiles(), knowledgeMetric);
				if(covarage < 0.5) 
					break;
				topContributors.add(projectVersion.getContributors().get(0));
				projectVersion.getContributors().remove(0);
				tf = tf+1;
			}
			log.info("SAVING TF DATA OF"+projectName);
			if(project.getId() == null) {
				projectRepository.save(project);
			}
			if(projectVersionRepository.existsByVersionId(projectVersion.getVersionId()) == false) {
				projectVersion.setProject(project);
				projectVersionRepository.save(projectVersion);
				TruckFactor truckFactor = new TruckFactor(tf, projectVersion, knowledgeMetric);
				truckFactorRepository.save(truckFactor);
				for (Contributor contributor : topContributors) {
					TruckFactorDevelopers truckFactorDevelopers = new TruckFactorDevelopers(contributor.getName(), contributor.getEmail(),
							truckFactor, (double)contributor.getNumberFilesAuthor()/(double)projectVersion.getNumberAnalysedFiles());
					if (truckFactorDevelopersRepository.
							existsByTruckFactorIdAndNameAndEmail(truckFactor.getId(), contributor.getName(),
									contributor.getEmail()) == false) {
						truckFactorDevelopersRepository.save(truckFactorDevelopers);
					}
				}
			}
		}
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

	protected int numberOfCommitsOtherDevsContributorFile(Contributor contributor, 
			File file, List<Commit> commits) {
		int numerCommits = 0;
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
			if (present == false) {
				for (CommitFile commitFile: commit.getCommitFiles()) {
					if (commitFile.getFile().getPath().equals(file.getPath())) {
						numerCommits++;
						break;
					}
				}
			}
		}
		return numerCommits;
	}

	protected int numberOfCommitsContributorFile(Contributor contributor, 
			File file, List<Commit> commits) {
		int numerCommits = 0;
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
						numerCommits++;
						break;
					}
				}
			}
		}
		return numerCommits;
	}


	public void directoriesTruckFactorAnalyzes(RepositoryKnowledgeMetricDTO request) throws IOException, 
	NoHeadException, GitAPIException{
		java.io.File dir = new java.io.File(request.getPath());
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				RepositoryKnowledgeMetricDTO repo = new RepositoryKnowledgeMetricDTO(projectPath, request.getKnowledgeMetric());
				projectTruckFactorAnalyzes(repo);
			}
		}
	}

	protected List<Contributor> extractAuthor(List<RevCommit> commitsList) {
		List<Contributor> contributors = new ArrayList<Contributor>();
		for (RevCommit jgitCommit: commitsList) {
			String nome = null, email = null;
			if (jgitCommit.getAuthorIdent() != null) {
				if (jgitCommit.getAuthorIdent().getEmailAddress() != null) {
					email = jgitCommit.getAuthorIdent().getEmailAddress();
				}else {
					email = jgitCommit.getCommitterIdent().getEmailAddress();
				}
				if (jgitCommit.getAuthorIdent().getName() != null) {
					nome = jgitCommit.getAuthorIdent().getName();
				}else {
					nome = jgitCommit.getCommitterIdent().getName();
				}
			}else {
				email = jgitCommit.getCommitterIdent().getEmailAddress();
				nome = jgitCommit.getCommitterIdent().getName();
			}
			Contributor author = new Contributor(nome, email);
			boolean present = false;
			for (Contributor contributor : contributors) {
				if (contributor.equals(author)) {
					present = true;
				}
			}
			if (present == false) {
				contributors.add(author);
			}
		}
		return contributors;
	}

	protected List<RevCommit> extractAllCommits(Git git){
		try {
			Iterable<RevCommit> commitsIterable = git.log().call();
			List<RevCommit> commitsList = new ArrayList<RevCommit>();
			commitsIterable.forEach(commitsList::add);
			return commitsList;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	protected double getCoverage(List<Contributor> contributors, List<File> files, KnowledgeMetric knowledgeMetric) {
		int fileSize = files.size();
		int numberFilesCovarage = 0;
		forFiles:for(File file: files) {
			if (file.getMantainers().size() > 0) {
				for (Contributor maintainer : file.getMantainers()) {
					for (Contributor contributor : contributors) {
						if (maintainer.equals(contributor)) {
							numberFilesCovarage++;
							continue forFiles;
						}
					}
				}
			}
		}
		double coverage = (double)numberFilesCovarage/(double)fileSize;
		return coverage; 
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
}
