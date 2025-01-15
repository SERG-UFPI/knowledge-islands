package br.com.gitanalyzer.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.SharedLinkCommitService;

@RestController
@RequestMapping("/api/shared-link-commit")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class SharedLinkCommitController {
	
	@Autowired
	private SharedLinkCommitService service;

	@PostMapping("/shared-link-commits-char")
	public ResponseEntity<?> sharedLinkCommitsChar() throws Exception{
		return ResponseEntity.ok(service.sharedLinkCommitsChar());
	}
	
	@GetMapping("/code-cpy-analysis")
	public ResponseEntity<?> codeCopyAnalysis(){
		service.codeCopyAnalysis();
		return ResponseEntity.ok("Finished");
	}
	
}
