package br.com.gitanalyzer.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.dto.form.GitRepositoryVersionKnowledgeModelForm2;
import br.com.gitanalyzer.service.GitRepositoryVersionKnowledgeModelService;

@RestController
@RequestMapping("/api/git-repository-version-knowledge-model")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class GitRepositoryVersionKnowledgeModelController {

	@Autowired
	private GitRepositoryVersionKnowledgeModelService service;

	@PostMapping("/save-git-repository-version-knowledge-model")
	public ResponseEntity<?> saveGitRepositoryVersionKnowledgeModel(@RequestBody GitRepositoryVersionKnowledgeModelForm2 form) throws Exception{
		return ResponseEntity.ok(service.saveGitRepositoryVersionKnowledgeModel(service.convertModelForm1ModelForm2(form)));
	}

	@GetMapping("/git-repository-version/{id}")
	public ResponseEntity<?> getByGitRepositoryVersionId(@PathVariable("id") Long id) throws Exception{
		return ResponseEntity.ok(service.getByGitRepositoryVersionId(id));
	}
}
