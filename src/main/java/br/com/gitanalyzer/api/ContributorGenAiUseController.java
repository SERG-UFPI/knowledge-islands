package br.com.gitanalyzer.api;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.ContributorGenAiUseService;

@RestController
@RequestMapping("/api/contributor-genAi-use")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class ContributorGenAiUseController {

	@Autowired
	private ContributorGenAiUseService service;

	@PostMapping("/contributor-genAi-use")
	public ResponseEntity<String> createContributorGenAiUse() throws InterruptedException, IOException {
		service.createContributorGenAiUse();
		return ResponseEntity.ok("Finished");
	}

}
