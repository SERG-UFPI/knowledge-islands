package br.com.gitanalyzer.api;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.dto.FilteringProjectsDTO;
import br.com.gitanalyzer.dto.GenerateFolderProjectLogDTO;
import br.com.gitanalyzer.service.FilterGitRepositoryService;
import br.com.gitanalyzer.service.ProjectService;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

	@Autowired
	private ProjectService projectService;
	@Autowired
	private FilterGitRepositoryService filterProjectService;
	
	@PostMapping("set-download-version-date")
	public ResponseEntity<?> setDownloadVersionDate(@RequestBody String folderPath) throws IOException{
		projectService.setProjectDatesFolder(folderPath);
		return ResponseEntity.ok("");
	}

	@PostMapping("/set-languages")
	public ResponseEntity<?> setProjectLanguages(){
		return ResponseEntity.ok(projectService.setProjectsMainLanguage());
	}

	@PostMapping("/create-folder-project-log")
	public ResponseEntity<?> createFolderProjectLog(@RequestBody GenerateFolderProjectLogDTO form) throws IOException{
		return ResponseEntity.ok(projectService.createFolderProjectLogs(form));
	}

	@PostMapping("/filter-projects-folder")
	public ResponseEntity<?> filteringProjects(@RequestBody FilteringProjectsDTO form) throws URISyntaxException, IOException, InterruptedException{
		filterProjectService.filter(form);
		return ResponseEntity.status(HttpStatus.OK).body("Filtering finished");
	}

	@PostMapping("/filtering-eco-spring")
	public ResponseEntity<?> filteringProjectsEcoSpring() throws URISyntaxException, IOException, InterruptedException{
		filterProjectService.filterEcoSpring();
		return ResponseEntity.status(HttpStatus.OK).body("Filtering finished");
	}

	@PostMapping("/extract-version")
	public ResponseEntity<?> extractVersion(@RequestBody String folderPath){
		projectService.extractVersion(folderPath);
		return ResponseEntity.status(HttpStatus.OK).body("Extraction finished");
	}

	@PostMapping("generate-linguist-file")
	public ResponseEntity<?> generateLinguistFile(@RequestBody String projectPath){
		try {
			projectService.generateFileLists(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/generate-commit-file")
	public ResponseEntity<?> generateCommitFile(@RequestBody String projectPath){
		try {
			projectService.generateCommitFile(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/generate-commit-file-folder")
	public ResponseEntity<?> generateCommitFileFolder(@RequestBody String projectPath){
		try {
			projectService.generateCommitFileFolder(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/generate-commitFile-file")
	public ResponseEntity<?> generateCommitFileFile(@RequestBody String projectPath){
		try {
			projectService.generateCommitFileFile(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/generate-cloc-file")
	public ResponseEntity<?> generateClocFile(@RequestBody String projectPath){
		try {
			projectService.generateClocFile(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/generate-logs-folder")
	public ResponseEntity<?> generateLogsFolder(@RequestBody String projectPath){
		try {
			projectService.generateLogFilesFolder(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/generate-logs-project")
	public ResponseEntity<?> generateLogFiles(@RequestBody String projectPath){
		try {
			projectService.generateLogFiles(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/saveFirstDateCommit")
	public ResponseEntity<?> setFirstDateFolder(@RequestBody String projectPath){
		try {
			projectService.setFirstDateProject(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@PostMapping("/return-version-downloaded")
	public ResponseEntity<?> returnVersionDownloaded(){
		try {
			projectService.returnVersionDownloaded();
			return ResponseEntity.ok("");
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
