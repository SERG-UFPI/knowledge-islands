package br.com.gitanalyzer.controllers;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.main.TruckFactorAnalyzer;

@RestController
@RequestMapping("/truck-factor")
public class TruckFactorController {

	@Autowired
	private TruckFactorAnalyzer service;

	@PostMapping
	public ResponseEntity<?> analyzer(@RequestBody String path){
		try {
			service.directoriesTruckFactorAnalyzes(path);
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.CREATED).body("Analysis finished");
	}

	@GetMapping
	public ResponseEntity<?> analyzer(){
		return ResponseEntity.status(HttpStatus.OK).body("Analysis finished");
	}
}
