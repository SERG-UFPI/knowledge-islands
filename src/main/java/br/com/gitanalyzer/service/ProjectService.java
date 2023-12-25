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

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import br.com.gitanalyzer.dto.GenerateFolderProjectLogDTO;
import br.com.gitanalyzer.extractors.CommitExtractor;
import br.com.gitanalyzer.model.entity.Project;
import br.com.gitanalyzer.repository.ProjectRepository;
import br.com.gitanalyzer.utils.AsyncUtils;
import br.com.gitanalyzer.utils.Constants;

@Service
public class ProjectService {

	@Autowired
	private ProjectRepository projectRepository;
	@Value("${configuration.project-logs.path}")
	private String projectLogsFolder;

	public Project returnProjectByPath(String projectPath) {
		String projectName = extractProjectName(projectPath);
		Project project = projectRepository.findByName(projectName);
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

			Path sourceClocFile = Paths.get(path+Constants.clocFileName);
			Path targetClocFile = Paths.get(folderPath+Constants.clocFileName);
			Files.copy(sourceClocFile, targetClocFile);

			Path sourceFileList = Paths.get(path+Constants.allFilesFileName);
			Path targetFileList = Paths.get(folderPath+Constants.allFilesFileName);
			Files.copy(sourceFileList, targetFileList);

			Path sourceFileLinguist = Paths.get(path+Constants.linguistFileName);
			Path targetFileLinguist = Paths.get(folderPath+Constants.linguistFileName);
			Files.copy(sourceFileLinguist, targetFileLinguist);

			Path sourceCommit = Paths.get(path+Constants.commitFileName);
			Path targetCommit = Paths.get(folderPath+Constants.commitFileName);
			Files.copy(sourceCommit, targetCommit);

			Path sourceCommitFile = Paths.get(path+Constants.commitFileFileName);
			Path targetCommitFile = Paths.get(folderPath+Constants.commitFileFileName);
			Files.copy(sourceCommitFile, targetCommitFile);

			Path sourceDiff = Paths.get(path+Constants.diffFileName);
			Path targetDiff = Paths.get(folderPath+Constants.diffFileName);
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
		System.out.println("======= Generating logs from "+name+" =======");
		generateFileLists(projectPath);
		generateCommitFile(projectPath);
		generateCommitFileFile(projectPath);
		generateClocFile(projectPath);
	}

	public void generateLogFilesWithoutCloc(String projectPath) throws URISyntaxException, IOException, InterruptedException {
		String name = extractProjectName(projectPath);
		System.out.println("======= Generating logs from "+name+" =======");
		generateFileLists(projectPath);
		generateCommitFile(projectPath);
		generateCommitFileFile(projectPath);
	}

	public void generateCommitFile(String path) throws URISyntaxException, IOException, InterruptedException {
		String name = extractProjectName(path);
		System.out.println("Generating commit file of "+name);
		String pathCommitScript = ProjectService.class.getResource("/commit_log_script.sh").toURI().getPath();
		String command = "sh "+pathCommitScript+" "+path;
		ProcessBuilder pb = new ProcessBuilder(new String[]{"bash", "-l", "-c", command});
		pb.redirectErrorStream(true);
		Process process = pb.start();
		process.waitFor();
		System.out.println("End generation commit file");
	}

	public void generateCommitFileFolder(String folderPath) throws URISyntaxException, IOException, InterruptedException {
		java.io.File dir = new java.io.File(folderPath);
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				File commitFile = new File(projectPath+Constants.commitFileName);
				if(commitFile.exists() == false) {
					generateCommitFile(projectPath);
				}
			}
		}
	}

	public void generateCommitFileFile(String projectPath) throws URISyntaxException, IOException, InterruptedException {
		String name = extractProjectName(projectPath);
		System.out.println("Generating commitFile file of "+name);
		CommitExtractor commitExtractor = new CommitExtractor();
		commitExtractor.generateCommitFileFile(projectPath);
		System.out.println("End generation commitFile file");
	}

	public void generateClocFile(String projectPath) throws URISyntaxException, IOException, InterruptedException {
		String name = extractProjectName(projectPath);
		System.out.println("Generating cloc file of "+name);
		String pathClocScript = ProjectService.class.getResource("/cloc_script.sh").toURI().getPath();
		String command = "sh "+pathClocScript+" "+projectPath;
		ProcessBuilder pb = new ProcessBuilder(new String[]{"bash", "-l", "-c", command});
		pb.redirectErrorStream(true);
		Process process = pb.start();
		process.waitFor();
		System.out.println("End generation cloc file");
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
		System.out.println("Generating linguist file of "+name);
		String pathRubyScript = ProjectService.class.getResource("/linguist.rb").toURI().getPath();
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
		System.out.println("End generation linguist file");
	}

	public Object setProjectsMainLanguage() {
		HashMap<String, String> nameLanguage = new HashMap<String, String>();
		List<Project> projetos = projectRepository.findAll();
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
		for (Project project : projetos) {
			for(Map.Entry<String, String> set: nameLanguage.entrySet()) {
				if (set.getKey().contains(project.getName())) {
					project.setMainLanguage(set.getValue());
					break;
				}
			}
			projectRepository.save(project);
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
		//				System.out.println("EXTRACTING DATA FROM "+projectName);
		//				ProjectVersion version = projectVersionExtractor
		//						.extractProjectVersionOnlyNumbers(projectPath);
		//				project.setFirstCommitDate(version.getFirstCommitDate());
		//				version.setProject(project);
		//				projectVersionRepository.save(version);
		//				System.out.println("EXTRACTION FINISHED");
		//			}
		//		}
	}

	public void generateLogFilesFolder(String folderPath) throws URISyntaxException, IOException, InterruptedException {
		ExecutorService executorService = AsyncUtils.getExecutorServiceForLogs();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		java.io.File dir = new java.io.File(folderPath);
		System.out.println("======= Generating logs from folder "+folderPath+" =======");
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
					try {
						generateLogFiles(projectPath);
					} catch (URISyntaxException | IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}, executorService);
				futures.add(future);
			}
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		executorService.shutdown();
		System.out.println("======= End generating logs from folder "+folderPath+" =======");
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
		Project project = returnProjectByPath(projectPath);
		if(project.getFirstCommitDate() == null) {
			project.setFirstCommitDate(commitExtractor.getFirstCommitDate(projectPath));
			projectRepository.save(project);
		}
	}

	public void setDownloadVersionDate(String projectPath) throws IOException {
		CommitExtractor commitExtractor = new CommitExtractor();
		Project project = returnProjectByPath(projectPath);
		if(project.getDownloadVersionDate() == null) {
			project.setDownloadVersionDate(commitExtractor.getLastCommitDate(projectPath));
			projectRepository.save(project);
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
		fullName = fullName.replace("https://github.com/", "");
		fullName = fullName.replace(".git", "");
		return fullName;
	}

	public void returnVersionDownloaded() throws URISyntaxException, IOException, InterruptedException {
		List<Project> projects = projectRepository.findAll();
		for (Project project : projects) {
			checkOutProjectVersion(project.getCurrentPath(), project.getDownloadVersionHash());
		}
	}

	public void checkOutProjectVersion(String path, String hash)  throws URISyntaxException, 
	IOException, InterruptedException {
		String pathCheckoutScript = ProjectService.class.getResource("/checkout_script.sh").toURI().getPath();
		String command = "sh "+pathCheckoutScript+" "+path+" "+hash;
		ProcessBuilder pb = new ProcessBuilder(new String[]{"bash", "-l", "-c", command});
		pb.redirectErrorStream(true);
		Process process = pb.start();
		process.waitFor();
	}

}