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

import br.com.gitanalyzer.main.TruckFactorHistoryAnalyzer;

@RestController
@RequestMapping("/truck-factor-history")
public class TruckFactorHistoryController {

	@Autowired
	private TruckFactorHistoryAnalyzer historyService;

	@PostMapping
	public ResponseEntity<?> analyzerHistory(@RequestBody String path){
		try {
			historyService.directoriesTruckFactorHistoryAnalyzes(path);
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.CREATED).body("Analysis finished");
	}

}
