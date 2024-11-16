package br.com.gitanalyzer.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import br.com.gitanalyzer.dto.GenerateFolderProjectLogDTO;
import br.com.gitanalyzer.extractors.CommitExtractor;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.repository.FileRepositorySharedLinkCommitRepository;
import br.com.gitanalyzer.repository.GitRepositoryRepository;
import br.com.gitanalyzer.utils.KnowledgeIslandsUtils;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class GitRepositoryService {

	@Autowired
	private GitRepositoryRepository repository;
	@Value("${configuration.project-logs.path}")
	private String projectLogsFolder;
	@Value("${configuration.permanent-clone.path}")
	private String permanentClonePath;
	@Autowired
	private FileRepositorySharedLinkCommitRepository fileGitRepositorySharedLinkCommitRepository;

	public GitRepository returnProjectByPath(String projectPath) {
		String projectName = extractProjectName(projectPath);
		GitRepository project = repository.findByName(projectName);
		return project;
	}

	public void deleteProjectFolder(String pathFolder) throws IOException {
		File directory = new File(pathFolder);
		org.apache.commons.io.FileUtils.deleteDirectory(directory); 
	}

	public void createFolderLogsAndCopyFiles(String path, String projectName, String versionId) {
		try {
			if(path.substring(path.length()-1).equals("/") == false) {
				path = path+"/";
			}
			String folderPath = createFolderProjectLogs(GenerateFolderProjectLogDTO.builder().projectName(projectName).versionId(versionId).build());

			Path sourceClocFile = Paths.get(path+KnowledgeIslandsUtils.clocFileName);
			Path targetClocFile = Paths.get(folderPath+KnowledgeIslandsUtils.clocFileName);
			Files.copy(sourceClocFile, targetClocFile);

			Path sourceFileList = Paths.get(path+KnowledgeIslandsUtils.allFilesFileName);
			Path targetFileList = Paths.get(folderPath+KnowledgeIslandsUtils.allFilesFileName);
			Files.copy(sourceFileList, targetFileList);

			Path sourceFileLinguist = Paths.get(path+KnowledgeIslandsUtils.linguistFileName);
			Path targetFileLinguist = Paths.get(folderPath+KnowledgeIslandsUtils.linguistFileName);
			Files.copy(sourceFileLinguist, targetFileLinguist);

			Path sourceCommit = Paths.get(path+KnowledgeIslandsUtils.commitFileName);
			Path targetCommit = Paths.get(folderPath+KnowledgeIslandsUtils.commitFileName);
			Files.copy(sourceCommit, targetCommit);

			Path sourceCommitFile = Paths.get(path+KnowledgeIslandsUtils.commitFileFileName);
			Path targetCommitFile = Paths.get(folderPath+KnowledgeIslandsUtils.commitFileFileName);
			Files.copy(sourceCommitFile, targetCommitFile);

			Path sourceDiff = Paths.get(path+KnowledgeIslandsUtils.diffFileName);
			Path targetDiff = Paths.get(folderPath+KnowledgeIslandsUtils.diffFileName);
			Files.copy(sourceDiff, targetDiff);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String createFolderProjectLogs(GenerateFolderProjectLogDTO form) throws IOException {
		String folderName = form.getProjectName()+"+"+form.getVersionId();
		String fullPath = projectLogsFolder+folderName;
		Files.createDirectory(Paths.get(fullPath));
		return fullPath+"/";
	}

	public void generateLogFiles(String projectPath) throws URISyntaxException, IOException, InterruptedException {
		String name = extractProjectName(projectPath);
		log.info("======= Generating logs from "+name+" =======");
		generateFileLists(projectPath);
		generateCommitFile(projectPath);
		generateCommitFileFile(projectPath);
		generateClocFile(projectPath);
	}

	public void generateLogFilesWithoutCloc(String projectPath) throws URISyntaxException, IOException, InterruptedException {
		String name = extractProjectName(projectPath);
		log.info("======= Generating logs from "+name+" =======");
		generateFileLists(projectPath);
		generateCommitFile(projectPath);
		generateCommitFileFile(projectPath);
	}

	public void generateCommitFile(String path) throws URISyntaxException, IOException, InterruptedException {
		String name = extractProjectName(path);
		log.info("Generating commit file of "+name);
		String pathCommitScript = GitRepositoryService.class.getResource("/scripts_shell/commit_log_script.sh").toURI().getPath();
		String command = "sh "+pathCommitScript+" "+path;
		ProcessBuilder pb = new ProcessBuilder(new String[]{"bash", "-l", "-c", command});
		pb.redirectErrorStream(true);
		Process process = pb.start();
		process.waitFor();
		log.info("End generation commit file");
	}

	public void generateCommitFileFolder(String folderPath) throws URISyntaxException, IOException, InterruptedException {
		ExecutorService executorService = KnowledgeIslandsUtils.getExecutorServiceMax();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		java.io.File dir = new java.io.File(folderPath);
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				File commitFile = new File(projectPath+KnowledgeIslandsUtils.commitFileName);
				//if(commitFile.exists() == false) {
				CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
					try {
						generateCommitFile(projectPath);
					} catch (URISyntaxException | IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}, executorService);
				futures.add(future);
				//}
			}
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		executorService.shutdown();
	}

	public void generateCommitFileFile(String projectPath) throws URISyntaxException, IOException, InterruptedException {
		String name = extractProjectName(projectPath);
		log.info("Generating commitFile file of "+name);
		CommitExtractor commitExtractor = new CommitExtractor();
		commitExtractor.generateCommitFileFile(projectPath);
		log.info("End generation commitFile file");
	}

	public void generateClocFile(String projectPath) throws URISyntaxException, IOException, InterruptedException {
		String name = extractProjectName(projectPath);
		log.info("Generating cloc file of "+name);
		String pathClocScript = GitRepositoryService.class.getResource("/scripts_shell/cloc_script.sh").toURI().getPath();
		String command = "sh "+pathClocScript+" "+projectPath;
		ProcessBuilder pb = new ProcessBuilder(new String[]{"bash", "-l", "-c", command});
		pb.redirectErrorStream(true);
		Process process = pb.start();
		process.waitFor();
		log.info("End generation cloc file");
	}

	public String getCurrentRevisionHash(String projectPath) throws IOException {
		String command = "cd "+projectPath+" && git rev-parse HEAD";
		ProcessBuilder pb = new ProcessBuilder(new String[] {"bash", "-l", "-c", command});
		pb.redirectErrorStream(true);
		Process process = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String hash = reader.readLine();
		return hash;
	}

	public void generateFileLists(String path) throws URISyntaxException, IOException, InterruptedException {
		String name = extractProjectName(path);
		log.info("Generating linguist file of "+name);
		String pathRubyScript = GitRepositoryService.class.getResource("/scripts_shell/linguist.rb").toURI().getPath();
		String command = "ruby "+pathRubyScript+" "+path;
		ProcessBuilder pb = new ProcessBuilder(new String[]{"bash", "-l", "-c", command});
		pb.redirectErrorStream(true);
		Process process = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		File file = new File(path+"/linguistfiles.log");
		FileOutputStream fos = new FileOutputStream(file);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		while ((line = reader.readLine()) != null) {
			bw.write(line);
			bw.newLine();
		}
		process.waitFor();
		bw.close();
		reader.close();
		fos.close();
		log.info("End generation linguist file");
	}

	public Object setProjectsMainLanguage() {
		HashMap<String, String> nameLanguage = new HashMap<String, String>();
		List<GitRepository> projetos = repository.findAll();
		List<String[]> projectsFile = null;
		try (CSVReader reader = new CSVReader(new FileReader("/home/otavio/Desktop/shell_scripts/rep-info-new.csv"))) {
			projectsFile = reader.readAll();
		} catch (IOException | CsvException e) {
			e.printStackTrace();
		}
		for (String[] string : projectsFile) {
			String name = string[0];
			String language = string[1];
			nameLanguage.put(name, language);
		}
		for (GitRepository project : projetos) {
			for(Map.Entry<String, String> set: nameLanguage.entrySet()) {
				if (set.getKey().contains(project.getName())) {
					project.setLanguage(set.getValue());
					break;
				}
			}
			repository.save(project);
		}
		return null;
	}

	public void extractVersion(String folderPath) {
		//		ProjectVersionExtractor projectVersionExtractor = new ProjectVersionExtractor();
		//		java.io.File dir = new java.io.File(folderPath);
		//		for (java.io.File fileDir: dir.listFiles()) {
		//			if (fileDir.isDirectory()) {
		//				String projectPath = fileDir.getAbsolutePath()+"/";
		//				String projectName = projectUtils.extractProjectName(projectPath);
		//				Project project = projectRepository.findByName(projectName);
		//				log.info("EXTRACTING DATA FROM "+projectName);
		//				ProjectVersion version = projectVersionExtractor
		//						.extractProjectVersionOnlyNumbers(projectPath);
		//				project.setFirstCommitDate(version.getFirstCommitDate());
		//				version.setProject(project);
		//				projectVersionRepository.save(version);
		//				log.info("EXTRACTION FINISHED");
		//			}
		//		}
	}

	public void generateLogFilesFolder(String folderPath) {
		List<String> repositoriesPaths = new ArrayList<>();
		java.io.File dir = new java.io.File(folderPath);
		log.info("======= Generating logs from folder "+folderPath+" =======");
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				repositoriesPaths.add(projectPath);
			}
		}
		generateLogFilesRepositoriesPaths(repositoriesPaths);
		log.info("======= End generating logs from folder "+folderPath+" =======");
	}

	public void generateLogFilesRepositoriesPaths(List<String> paths) {
		ExecutorService executorService = KnowledgeIslandsUtils.getExecutorServiceMax();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (String repositoryPath: paths) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
				try {
					generateLogFiles(repositoryPath);
					GitRepository gitRepository = repository.findByCurrentFolderPath(repositoryPath);
					if(gitRepository != null) {
						gitRepository.setGeneratedLogs(true);
						repository.save(gitRepository);
					}
				} catch (URISyntaxException | IOException | InterruptedException e) {
					e.printStackTrace();
					log.error(e.getMessage());
				}
			}, executorService);
			futures.add(future);
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		executorService.shutdown();
	}

	public void generateLogFilesFolderWithoutCloc(String folderPath) throws URISyntaxException, IOException, InterruptedException {
		java.io.File dir = new java.io.File(folderPath);
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				generateLogFilesWithoutCloc(projectPath);
			}
		}
	}

	public void setProjectDatesFolder(String folderPath) throws IOException {
		java.io.File dir = new java.io.File(folderPath);
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				setFirstDateProject(projectPath);
				setDownloadVersionDate(projectPath);
			}
		}
	}

	public void setFirstDateProject(String projectPath) throws IOException {
		CommitExtractor commitExtractor = new CommitExtractor();
		GitRepository project = returnProjectByPath(projectPath);
		if(project != null && project.getFirstCommitDate() == null) {
			project.setFirstCommitDate(commitExtractor.getFirstCommitDate(projectPath));
			repository.save(project);
		}
	}

	public void setDownloadVersionDate(String projectPath) throws IOException {
		CommitExtractor commitExtractor = new CommitExtractor();
		GitRepository project = returnProjectByPath(projectPath);
		if(project != null && project.getDownloadDate() == null) {
			project.setDownloadDate(commitExtractor.getLastCommitDate(projectPath));
			repository.save(project);
		}
	}

	public String extractProjectName(String path) {
		String fileSeparator = File.separator;
		String[] splitedPath = path.split("\\"+fileSeparator);
		String projectName = splitedPath[splitedPath.length - 1];
		return projectName;
	}

	public String extractProjectFullName(String path) throws IOException {
		String command = "cd "+path+" && git config --get remote.origin.url";
		ProcessBuilder pb = new ProcessBuilder(new String[] {"bash", "-l", "-c", command});
		pb.redirectErrorStream(true);
		Process process = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String fullName = reader.readLine();
		fullName = fullName.replace(KnowledgeIslandsUtils.gitHubBaseUrl, "");
		fullName = fullName.replace(".git", "");
		return fullName;
	}

	public void returnVersionDownloaded() throws URISyntaxException, IOException, InterruptedException {
		List<GitRepository> projects = repository.findAll();
		for (GitRepository project : projects) {
			checkOutProjectVersion(project.getCurrentFolderPath(), project.getDownloadVersionHash());
		}
	}

	public void checkOutProjectVersion(String path, String hash)  throws URISyntaxException, 
	IOException, InterruptedException {
		String pathCheckoutScript = GitRepositoryService.class.getResource("/scripts_shell/checkout_script.sh").toURI().getPath();
		String command = "sh "+pathCheckoutScript+" "+path+" "+hash;
		ProcessBuilder pb = new ProcessBuilder(new String[]{"bash", "-l", "-c", command});
		pb.redirectErrorStream(true);
		Process process = pb.start();
		process.waitFor();
	}

	public void deleteDownloadedRepos() {
		try {
			File directory = new File(permanentClonePath);
			org.apache.commons.io.FileUtils.deleteDirectory(directory);
			directory.mkdir();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Transactional
	public GitRepository saveGitRepository(String repositoryPath) throws IOException {
		log.info("BEGIN SAVING GIT REPOSITORY: "+repositoryPath);
		String projectName = extractProjectName(repositoryPath);
		GitRepository gitRepository = null;
		if(repository.existsByName(projectName)) {
			return repository.findByName(projectName);
		}else if(repository.existsByCurrentFolderPath(repositoryPath)){
			return repository.findByCurrentFolderPath(repositoryPath);
		}else{
			try {
				gitRepository = new GitRepository(projectName, repositoryPath, 
						extractProjectFullName(repositoryPath), getCurrentRevisionHash(repositoryPath));
			} catch (Exception e) {
				gitRepository = new GitRepository(projectName, repositoryPath, 
						null, getCurrentRevisionHash(repositoryPath));
			}
			log.info("ENDING SAVING GIT REPOSITORY: "+repositoryPath);
			return repository.save(gitRepository);
		}
	}

	public void removeAll() {
		repository.deleteAll();
	}

	public void generateLogsProjectsSharedLink() {
		List<GitRepository> repositories = fileGitRepositorySharedLinkCommitRepository.findDistinctGitRepositoriesWithNonNullConversationAndCurrentFolderPathIsNotNull();
		List<String> paths = repositories.stream().map(r -> r.getCurrentFolderPath()).toList();
		generateLogFilesRepositoriesPaths(paths);
	}

}