package br.com.gitanalyzer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.FilterProjectService;
import br.com.gitanalyzer.service.ProjectService;

@RestController
@RequestMapping("/project")
public class ProjectController {

	@Autowired
	private ProjectService service;
	@Autowired
	private FilterProjectService filterProjectService;

	@PostMapping
	public ResponseEntity<?> setProjectLanguages(){
		return ResponseEntity.ok(service.setProjectsMainLanguage());
	}

	@PostMapping
	public ResponseEntity<?> filteringProjects(@RequestBody String folderPath){
		filterProjectService.filter(folderPath);
		return ResponseEntity.status(HttpStatus.OK).body("Filtering finished");
	}
}
