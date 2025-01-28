package br.com.knowledgeislands.api;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledgeislands.service.ContributorGenAiUseService;

@RestController
@RequestMapping("/api/contributor-genAi-use")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class ContributorGenAiUseController {

	@Autowired
	private ContributorGenAiUseService service;

	@PostMapping("/contributor-genAi-use-shared-link")
	public ResponseEntity<String> createContributorGenAiUseSharedLink() throws InterruptedException, IOException {
		service.createContributorGenAiUseSharedLink();
		return ResponseEntity.ok("Finished");
	}
	
	@PostMapping("/contributor-genAi-use")
	public ResponseEntity<String> createContributorGenAiUse() throws InterruptedException, IOException {
		service.createContributorGenAiUse();
		return ResponseEntity.ok("Finished");
	}
	
	@PostMapping("/fix-contributor-genAi-use")
	public ResponseEntity<String> fixCreateContributorGenAiUse() throws InterruptedException, IOException {
		service.fixCreateContributorGenAiUse();
		return ResponseEntity.ok("Finished");
	}
	
	@PostMapping("/contributor-genAi-use-full")
	public ResponseEntity<String> createContributorGenAiUseFull() throws InterruptedException, IOException {
		service.createContributorGenAiUseFull();
		return ResponseEntity.ok("Finished");
	}

}
