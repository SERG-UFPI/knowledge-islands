package br.com.gitanalyzer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.main.dto.HashNumberYears;
import br.com.gitanalyzer.service.HistoryCommitsService;

@RestController
@RequestMapping("/api/history-commits")
public class HistoryCommits {

	@Autowired
	private HistoryCommitsService service;

	@PostMapping("history-folder")
	public ResponseEntity<?> saveHistoryFolder(@RequestBody HashNumberYears form){
		service.commitsHashsFolder(form);
		return ResponseEntity.ok(HttpStatus.OK);
	}

	@PostMapping("history-project")
	public ResponseEntity<?> saveHistoryProject(@RequestBody HashNumberYears form){
		service.commitsHashsProject(form);
		return ResponseEntity.ok(HttpStatus.OK);
	}
}
