package br.com.gitanalyzer.controllers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.CommitService;
import br.com.gitanalyzer.service.FilterProjectService;
import br.com.gitanalyzer.service.ProjectService;

@RestController
@RequestMapping("/project")
public class ProjectController {

	@Autowired
	private ProjectService projectService;
	@Autowired
	private FilterProjectService filterProjectService;
	@Autowired
	private CommitService commitService;

	@PostMapping("/set-languages")
	public ResponseEntity<?> setProjectLanguages(){
		return ResponseEntity.ok(projectService.setProjectsMainLanguage());
	}

	@PostMapping("/filtering")
	public ResponseEntity<?> filteringProjects(@RequestBody String folderPath){
		filterProjectService.filter(folderPath);
		return ResponseEntity.status(HttpStatus.OK).body("Filtering finished");
	}

	@PostMapping("extract-version")
	public ResponseEntity<?> extractVersion(@RequestBody String folderPath){
		projectService.extractVersion(folderPath);
		return ResponseEntity.status(HttpStatus.OK).body("Extraction finished");
	}
	
	@PostMapping("generate-linguist-file")
	public ResponseEntity<?> generateLinguistFile(@RequestBody String projectPath){
		try {
			commitService.generateFileLists(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@PostMapping("generate-commit-file")
	public ResponseEntity<?> generateCommitFile(@RequestBody String projectPath){
		try {
			commitService.generateCommitFile(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@PostMapping("generate-commitFile-file")
	public ResponseEntity<?> generateCommitFileFile(@RequestBody String projectPath){
		try {
			commitService.generateCommitFileFile(projectPath);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
