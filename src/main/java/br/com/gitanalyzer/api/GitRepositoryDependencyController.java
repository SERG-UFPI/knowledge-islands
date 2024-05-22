package br.com.gitanalyzer.api;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.GitRepositoryDependencyService;

@RestController
@RequestMapping("/api/git-repository-dependency")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class GitRepositoryDependencyController {
	
	@Autowired
	private GitRepositoryDependencyService projectDependencyService;
	
	@PostMapping("/set-dependencies_project")
	public ResponseEntity<?> extractVersion(@RequestBody Long id) throws IOException, InterruptedException{
		projectDependencyService.getProjectVersionAndSetDependency(id);
		return ResponseEntity.status(HttpStatus.OK).body("Extraction finished");
	}
	
}
