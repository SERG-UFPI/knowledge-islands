package br.com.gitanalyzer.api;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.dto.FilteringProjectsDTO;
import br.com.gitanalyzer.dto.GenerateFolderProjectLogDTO;
import br.com.gitanalyzer.service.FilterGitRepositoryService;
import br.com.gitanalyzer.service.GitRepositoryService;

@RestController
@RequestMapping("/api/git-repository")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class GitRepositoryController {

	@Autowired
	private GitRepositoryService gitRepositoryService;
	@Autowired
	private FilterGitRepositoryService filterProjectService;

	@PostMapping("delete-downloaded-repos")
	public ResponseEntity<?> deleteDownloadedRepos() throws IOException{
		gitRepositoryService.deleteDownloadedRepos();
		return ResponseEntity.ok("");
	}

	@PostMapping("set-download-version-date")
	public ResponseEntity<?> setDownloadVersionDate(@RequestBody String folderPath) throws IOException{
		gitRepositoryService.setProjectDatesFolder(folderPath);
		return ResponseEntity.ok("");
	}

	@PostMapping("/set-languages")
	public ResponseEntity<?> setProjectLanguages(){
		return ResponseEntity.ok(gitRepositoryService.setProjectsMainLanguage());
	}

	@PostMapping("/create-folder-project-log")
	public ResponseEntity<?> createFolderProjectLog(@RequestBody GenerateFolderProjectLogDTO form) throws IOException{
		return ResponseEntity.ok(gitRepositoryService.createFolderProjectLogs(form));
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
		gitRepositoryService.extractVersion(folderPath);
		return ResponseEntity.status(HttpStatus.OK).body("Extraction finished");
	}

	@PostMapping("generate-linguist-file")
	public ResponseEntity<?> generateLinguistFile(@RequestBody String projectPath){
		try {
			gitRepositoryService.generateFileLists(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/generate-commit-file")
	public ResponseEntity<?> generateCommitFile(@RequestBody String projectPath){
		try {
			gitRepositoryService.generateCommitFile(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/generate-commit-file-folder")
	public ResponseEntity<?> generateCommitFileFolder(@RequestBody String projectPath){
		try {
			gitRepositoryService.generateCommitFileFolder(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/generate-commitFile-file")
	public ResponseEntity<?> generateCommitFileFile(@RequestBody String projectPath){
		try {
			gitRepositoryService.generateCommitFileFile(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/generate-cloc-file")
	public ResponseEntity<?> generateClocFile(@RequestBody String projectPath){
		try {
			gitRepositoryService.generateClocFile(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/generate-logs-folder")
	public ResponseEntity<?> generateLogsFolder(@RequestBody String projectPath){
		gitRepositoryService.generateLogFilesFolder(projectPath);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PostMapping("/generate-logs-projects-shared-link")
	public ResponseEntity<?> generateLogsProjectsSharedLink(){
		gitRepositoryService.generateLogsProjectsSharedLink();
		return ResponseEntity.ok("Finished");
	}

	@PostMapping("/generate-logs-repository")
	public ResponseEntity<?> generateLogFiles(@RequestBody String projectPath){
		try {
			gitRepositoryService.generateLogFiles(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/saveFirstDateCommit")
	public ResponseEntity<?> setFirstDateFolder(@RequestBody String projectPath){
		try {
			gitRepositoryService.setFirstDateProject(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/return-version-downloaded")
	public ResponseEntity<?> returnVersionDownloaded(){
		try {
			gitRepositoryService.returnVersionDownloaded();
			return ResponseEntity.ok("");
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/save-git-repository")
	public ResponseEntity<?> saveGitRepository(@RequestBody String repositoryPath){
		try {
			return ResponseEntity.ok(gitRepositoryService.saveGitRepository(repositoryPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@DeleteMapping("/all")
	public ResponseEntity<?> removeAll(){
		gitRepositoryService.removeAll();
		return ResponseEntity.ok("Ok");
	}

}
