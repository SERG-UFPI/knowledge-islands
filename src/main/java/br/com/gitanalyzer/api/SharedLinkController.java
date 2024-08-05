package br.com.gitanalyzer.api;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.exceptions.NoCommitForFileException;
import br.com.gitanalyzer.service.SharedLinkService;

@RestController
@RequestMapping("/api/shared-link")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class SharedLinkController {

	@Autowired
	private SharedLinkService service;

	@PostMapping("/create-file-shared-links-full")
	public ResponseEntity<?> createFileSharedLinksFull() throws Exception{
		service.saveFileSharedLinkFull();
		return ResponseEntity.ok("Shared links downloaded");
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

	@PostMapping("/create-repos-file-shared-links")
	public ResponseEntity<?> createReposFileSharedLinks() throws Exception{
		service.saveReposFileSharedLinks();
		return ResponseEntity.ok("Shared links downloaded");
	}

	@PostMapping("/set-commits-of-files")
	public ResponseEntity<?> setCommitsOfFiles() throws NoHeadException, IOException, GitAPIException, NoCommitForFileException{
		return ResponseEntity.ok(service.setCommitsOfFilesSharedLinks());
	}

}
