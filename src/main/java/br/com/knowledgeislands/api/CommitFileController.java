package br.com.knowledgeislands.api;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledgeislands.service.CommitFileService;

@RestController
@RequestMapping("/api/commit-file")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class CommitFileController {
	
	@Autowired
	private CommitFileService service;

	@PostMapping("/set-commit-file-genai")
	public ResponseEntity<String> setCommitFileGenai() throws InterruptedException, IOException {
		service.setCommitFileGenai();
		return ResponseEntity.ok("Finished");
	}
}
