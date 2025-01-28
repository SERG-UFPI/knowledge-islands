package br.com.knowledgeislands.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledgeislands.service.GitRepositoryVersionKnowledgeModelGenAiService;

@RestController
@RequestMapping("/api/git-repository-version-knowledge-model-gen-ai")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class GitRepositoryVersionKnowledgeModelGenAiController {

	@Autowired
	private GitRepositoryVersionKnowledgeModelGenAiService service;

	@PostMapping("/create-percentages")
	public ResponseEntity<String> createPercentages() throws Exception{
		service.createGitRepositoryVersionKnowledgeModelGenAi();
		return ResponseEntity.ok("Finished");
	}
}
