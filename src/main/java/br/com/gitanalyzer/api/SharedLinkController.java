package br.com.gitanalyzer.api;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.SharedLinkService;
import br.com.gitanalyzer.utils.KnowledgeIslandsUtils;

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
	public ResponseEntity<?> createFileSharedLinks() {
		for (String term : KnowledgeIslandsUtils.getChatGPTSearchTerms()) {
			service.saveFileSharedLinks(term);
		}
		return ResponseEntity.ok("Finished");
	}

	@PostMapping("/set-conversation-shared-links")
	public ResponseEntity<?> setConversationSharedLinks() {
		service.setConversationSharedLinks();
		return ResponseEntity.ok("Finished");
	}

	@PostMapping("/create-links-conversations-repo-info")
	public ResponseEntity<String> createSharedLinkConversationRepoInfo() throws InterruptedException, IOException {
		service.createSharedLinkConversationRepoInfo();
		return ResponseEntity.ok("Finished");
	}

	@GetMapping("/number_shared_links_per_language")
	public ResponseEntity<?> numberSharedLinksPerLanguage() throws GitAPIException{
		return ResponseEntity.ok(service.numberSharedLinksPerLanguage());
	}

}
