package br.com.knowledgeislands.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledgeislands.dto.form.GitRepositoryVersionKnowledgeModelForm1;
import br.com.knowledgeislands.dto.form.GitRepositoryVersionKnowledgeModelForm2;
import br.com.knowledgeislands.exceptions.MachineLearningUseException;
import br.com.knowledgeislands.model.enums.KnowledgeModel;
import br.com.knowledgeislands.service.GitRepositoryVersionKnowledgeModelService;

@RestController
@RequestMapping("/api/git-repository-version-knowledge-model")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class GitRepositoryVersionKnowledgeModelController {

	@Autowired
	private GitRepositoryVersionKnowledgeModelService service;

	@PostMapping("/save-git-repository-version-knowledge-model")
	public ResponseEntity<?> saveGitRepositoryVersionKnowledgeModel(@RequestBody GitRepositoryVersionKnowledgeModelForm2 form) throws Exception{
		return ResponseEntity.ok(service.saveGitRepositoryVersionKnowledgeModel(service.convertModelForm1ModelForm2(form)));
	}

	@PostMapping("/save-git-repository-version-knowledge-model-not-filtered")
	public ResponseEntity<?> saveGitRepositoryVersionKnowledgeModelNotFiltered() throws Exception{
		service.saveGitRepositoryVersionKnowledgeModelNotFiltered();
		return ResponseEntity.ok("Finished");
	}
	
	@PostMapping("/save-git-repository-version-knowledge-model-not-filtered-doe")
	public ResponseEntity<?> saveGitRepositoryVersionKnowledgeModelNotFilteredDOE() throws Exception{
		service.saveGitRepositoryVersionKnowledgeModelNotFilteredDOE();
		return ResponseEntity.ok("Finished");
	}

	@PostMapping("/save-git-repository-version-knowledge-model-percentage")
	public ResponseEntity<?> saveGitRepositoryVersionKnowledgeModelPercentage(@RequestBody GitRepositoryVersionKnowledgeModelForm1 form) throws Exception{
		return ResponseEntity.ok(service.saveGitRepositoryVersionKnowledgeModel(form));
	}

	@GetMapping("/git-repository-version/{id}")
	public ResponseEntity<?> getByGitRepositoryVersionId(@PathVariable("id") Long id) throws Exception{
		return ResponseEntity.ok(service.getByGitRepositoryVersionId(id));
	}

	@PostMapping("/save-repository-version-knowledge-shared-links")
	public ResponseEntity<String> saveRepositoryVersionKnowledgeSharedLinks(@RequestBody KnowledgeModel knowledgeMetric) throws Exception{
		service.saveRepositoryVersionKnowledgeSharedLinks(knowledgeMetric);
		return ResponseEntity.ok("Finished");
	}

	@PostMapping("/save-repository-version-knowledge-shared-links-genai")
	public ResponseEntity<String> saveRepositoryVersionKnowledgeSharedLinksGenAi(@RequestBody KnowledgeModel knowledgeMetric) throws Exception{
		service.saveRepositoryVersionKnowledgeSharedLinksGenAi(knowledgeMetric);
		return ResponseEntity.ok("Finished");
	}

	@PostMapping("/save-repository-version-knowledge-genai-by-repository")
	public ResponseEntity<String> saveRepositoryVersionKnowledgeGenAiByRepositoryId(@RequestBody Long idGitRepository) throws MachineLearningUseException {
		service.saveRepositoryVersionKnowledgeGenAiByRepositoryId(idGitRepository);
		return ResponseEntity.ok("Finished");
	}

	@PostMapping("/save-repository-version-knowledge-genai")
	public ResponseEntity<String> saveRepositoryVersionKnowledgeGenAi() throws Exception{
		service.saveRepositoryVersionKnowledgeGenAi();
		return ResponseEntity.ok("Finished");
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> removeGitRepositoryVersionKnowledgeModel(@PathVariable Long id){
		service.removeGitRepositoryVersionKnowledgeModel(id);
		return ResponseEntity.ok("Finished");
	}

	//	@PostMapping("/save-repository-version-knowledge-shared-links-genai-full")
	//	public ResponseEntity<String> saveRepositoryVersionKnowledgeSharedLinksGenAiFull() throws Exception{
	//		service.saveRepositoryVersionKnowledgeSharedLinksGenAiFull();
	//		return ResponseEntity.ok("Finished");
	//	}
}
