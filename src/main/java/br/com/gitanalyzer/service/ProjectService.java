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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import br.com.gitanalyzer.dto.GenerateFolderProjectLogDTO;
import br.com.gitanalyzer.extractors.CommitExtractor;
import br.com.gitanalyzer.model.entity.Project;
import br.com.gitanalyzer.repository.ProjectRepository;
import br.com.gitanalyzer.utils.Constants;
import br.com.gitanalyzer.utils.ProjectUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProjectService {

	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private CommitService commitService;
	private ProjectUtils projectUtils = new ProjectUtils();
	@Value("${configuration.project-logs.path}")
	private String projectLogsFolder;

	public Project returnProjectByPath(String projectPath) {
		String projectName = projectUtils.extractProjectName(projectPath);
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
			log.error(e.getMessage());
		}
	}

	public String createFolderProjectLogs(GenerateFolderProjectLogDTO form) throws IOException {
		String folderName = form.getProjectName()+"+"+form.getVersionId();
		String fullPath = projectLogsFolder+folderName;
		Files.createDirectory(Paths.get(fullPath));
		return fullPath+"/";
	}

	public void generateLogFiles(String projectPath) throws URISyntaxException, IOException, InterruptedException {
		String name = projectUtils.extractProjectName(projectPath);
		log.info("======= Generating logs from "+name+" =======");
		generateFileLists(projectPath);
		commitService.generateCommitFile(projectPath);
		commitService.generateCommitFileFile(projectPath);
		generateClocFile(projectPath);
	}

	public void generateLogFilesWithoutCloc(String projectPath) throws URISyntaxException, IOException, InterruptedException {
		String name = projectUtils.extractProjectName(projectPath);
		log.info("======= Generating logs from "+name+" =======");
		generateFileLists(projectPath);
		commitService.generateCommitFile(projectPath);
		commitService.generateCommitFileFile(projectPath);
	}

	public void generateClocFile(String projectPath) throws URISyntaxException, IOException, InterruptedException {
		String name = projectUtils.extractProjectName(projectPath);
		log.info("Generating cloc file of "+name);
		String pathClocScript = CommitService.class.getResource("/cloc_script.sh").toURI().getPath();
		String command = "sh "+pathClocScript+" "+projectPath;
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();
		log.info("End generation cloc file");
	}

	public void generateFileLists(String path) throws URISyntaxException, IOException, InterruptedException {
		String name = projectUtils.extractProjectName(path);
		log.info("Generating linguist file of "+name);
		String pathRubyScript = CommitService.class.getResource("/linguist.rb").toURI().getPath();
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

	public void generateLogFilesFolder(String folderPath) throws URISyntaxException, IOException, InterruptedException {
		java.io.File dir = new java.io.File(folderPath);
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				generateLogFiles(projectPath);
			}
		}
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

	public void setFirstDateFolder(String folderPath) throws IOException {
		CommitExtractor commitExtractor = new CommitExtractor();
		java.io.File dir = new java.io.File(folderPath);
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				String projectName = projectUtils.extractProjectName(projectPath);
				Project project = projectRepository.findByName(projectName);
				project.setFirstCommitDate(commitExtractor.getFirstCommitDate(projectPath));
				projectRepository.save(project);
			}
		}
	}

}