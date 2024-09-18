package br.com.gitanalyzer.api;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.exceptions.NoCommitForFileException;
import br.com.gitanalyzer.service.SharedLinkService;

@RestController
@RequestMapping("/api/shared-link")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class SharedLinkController {

	@Autowired
	private SharedLinkService service;
	
	@PostMapping("/create-shared-link-full")
	public ResponseEntity<?> createSharedLinkFull() throws Exception{
		service.createSharedLinkFull();
		return ResponseEntity.ok("Finished");
	}
	
	@PostMapping("/save-git-repositories-api")
	public ResponseEntity<?> saveGitRepositoriesApi() throws Exception{
		return ResponseEntity.ok(service.saveGitRepositoriesApi());
	}
	
	@PostMapping("/create-file-shared-links")
	public ResponseEntity<?> createFileSharedLinks() throws Exception{
		return ResponseEntity.ok(service.saveFileSharedLinks());
	}

	@PostMapping("/set-conversation-shared-links")
	public ResponseEntity<?> setConversationSharedLinks() throws Exception{
		service.setConversationSharedLinks();
		return ResponseEntity.ok("Finished");
	}

	@GetMapping("/number_shared_links_per_language")
	public ResponseEntity<?> numberSharedLinksPerLanguage() throws NoHeadException, IOException, GitAPIException, NoCommitForFileException{
		return ResponseEntity.ok(service.numberSharedLinksPerLanguage());
	}

}
