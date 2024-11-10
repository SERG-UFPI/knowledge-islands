package br.com.gitanalyzer.api;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.FileRepositorySharedLinkCommitService;

@RestController
@RequestMapping("/api/file-repository-shared-link-commit")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class FileRepositorySharedLinkCommitController {
	
	@Autowired
	private FileRepositorySharedLinkCommitService service;

	@PostMapping("/export-file-repository-shared-link-commit")
	public ResponseEntity<?> exportFileRepositorySharedLinkCommit() throws IOException {
		service.exportFileRepositorySharedLinkCommit();
		return ResponseEntity.status(HttpStatus.CREATED).body("Finished");
	}

}
