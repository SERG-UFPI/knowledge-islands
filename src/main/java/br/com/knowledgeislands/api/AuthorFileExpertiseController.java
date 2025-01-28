package br.com.knowledgeislands.api;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledgeislands.service.AuthorFileExpertiseService;

@RestController
@RequestMapping("/api/author-file-expertise")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class AuthorFileExpertiseController {

	@Autowired
	private AuthorFileExpertiseService service;

	@PostMapping("/export-author-file-expertise-shared-link")
	public ResponseEntity<String> exportAuthorFileExpertiseSharedLink() throws InterruptedException, IOException {
		service.exportAuthorFileExpertiseSharedLink();
		return ResponseEntity.ok("Finished");
	}
}
