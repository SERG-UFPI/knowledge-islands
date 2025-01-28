package br.com.knowledgeislands.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledgeislands.dto.form.HashNumberYearsForm;
import br.com.knowledgeislands.service.HistoryCommitsService;

@RestController
@RequestMapping("/api/history-commits")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class HistoryCommits {

	@Autowired
	private HistoryCommitsService service;

	@PostMapping("history-folder")
	public ResponseEntity<?> saveHistoryFolder(@RequestBody HashNumberYearsForm form){
		service.commitsHashsFolder(form);
		return ResponseEntity.ok(HttpStatus.OK);
	}

	@PostMapping("history-project")
	public ResponseEntity<?> saveHistoryProject(@RequestBody HashNumberYearsForm form){
		service.commitsHashsProject(form);
		return ResponseEntity.ok(HttpStatus.OK);
	}
}
