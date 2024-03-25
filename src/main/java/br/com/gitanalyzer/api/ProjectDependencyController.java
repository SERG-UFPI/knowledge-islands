package br.com.gitanalyzer.api;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.ProjectDependencyService;

@RestController
@RequestMapping("/api/project-dependency")
public class ProjectDependencyController {
	
	@Autowired
	private ProjectDependencyService projectDependencyService;
	
	@PostMapping("/set-dependencies_project")
	public ResponseEntity<?> extractVersion(@RequestBody Long id) throws IOException, InterruptedException{
		projectDependencyService.getProjectVersionAndSetDependency(id);
		return ResponseEntity.status(HttpStatus.OK).body("Extraction finished");
	}
	
}
