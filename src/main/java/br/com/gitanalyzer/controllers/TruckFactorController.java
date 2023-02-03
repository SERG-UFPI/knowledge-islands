package br.com.gitanalyzer.controllers;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.main.TruckFactorAnalyzer;
import br.com.gitanalyzer.main.dto.RepositoryKnowledgeMetricDTO;

@RestController
@RequestMapping("/truck-factor")
public class TruckFactorController {

	@Autowired
	private TruckFactorAnalyzer service;

	@PostMapping("repos-truck-factor")
	public ResponseEntity<?> directoriesTruckFactorAnalyzes(@RequestBody RepositoryKnowledgeMetricDTO form){
		try {
			service.directoriesTruckFactorAnalyzes(form);
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.CREATED).body("Analysis finished");
	}
	
	@PostMapping("repo-truck-factor")
	public ResponseEntity<?> directoryTruckFactorAnalyzes(@RequestBody RepositoryKnowledgeMetricDTO form){
		try {
			service.projectTruckFactorAnalyzes(form);
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.CREATED).body("Analysis finished");
	}

}
