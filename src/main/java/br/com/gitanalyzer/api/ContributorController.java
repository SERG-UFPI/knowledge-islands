package br.com.gitanalyzer.api;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.ContributorService;

@RestController
@RequestMapping("/api/contributor")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class ContributorController {

	@Autowired
	private ContributorService service;

	@PostMapping("/send-emails-contributors-shared-links")
	public ResponseEntity<String> sendEmailsContributorsSharedLinks() throws InterruptedException, IOException {
		service.sendEmailsContributorsSharedLinks();
		return ResponseEntity.ok("Finished");
	}
}
