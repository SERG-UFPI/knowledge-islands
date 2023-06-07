package br.com.gitanalyzer.controllers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.dto.TruckFactorDTO;
import br.com.gitanalyzer.main.dto.CloneRepoForm;
import br.com.gitanalyzer.main.dto.HistoryReposTruckFactorDTO;
import br.com.gitanalyzer.main.dto.RepositoryKnowledgeMetricDTO;
import br.com.gitanalyzer.model.entity.TruckFactorProcess;
import br.com.gitanalyzer.service.TruckFactorService;

@RestController
@RequestMapping("/truck-factor")
public class TruckFactorController {

	@Autowired
	private TruckFactorService service;

	@PostMapping("repos-truck-factor")
	public ResponseEntity<?> directoriesTruckFactorAnalyzes(@RequestBody RepositoryKnowledgeMetricDTO form){
		try {
			service.directoriesTruckFactorAnalyzes(form);
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.OK).body("Analysis finished");
	}

	@PostMapping("clone-and-truck-factor")
	public ResponseEntity<TruckFactorProcess> cloneAndTruckFactor(@RequestBody CloneRepoForm form){
		return ResponseEntity.ok(service.cloneAndCalculateTruckFactor(form));
	}

	@PostMapping("repo-truck-factor")
	public ResponseEntity<TruckFactorDTO> directoryTruckFactorAnalyzes(@RequestBody RepositoryKnowledgeMetricDTO form){
		try {
			return ResponseEntity.ok(service.generateTruckFactorProject(form));
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("history-repos-truck-factor")
	public ResponseEntity<?> historyReposTruckFactor(@RequestBody HistoryReposTruckFactorDTO form) throws URISyntaxException, InterruptedException{
		try {
			service.historyReposTruckFactor(form);
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.OK).body("Analysis finished");
	}
}
