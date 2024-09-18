package br.com.gitanalyzer.api;

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

import br.com.gitanalyzer.service.GitRepositoryVersionService;

@RestController
@RequestMapping("/api/git-repository-version")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class GitRepositoryVersionController {

	@Autowired
	private GitRepositoryVersionService service;

	@DeleteMapping("/{id}")
	public ResponseEntity<?> remove(@PathVariable Long id){
		service.remove(id);
		return ResponseEntity.ok("Ok");
	}

	@DeleteMapping("/project/{id}")
	public ResponseEntity<?> removeFromProject(@PathVariable Long id){
		service.removeFromProject(id);
		return ResponseEntity.ok("ok");
	}

	@PostMapping("/remove-from-repos-filtered")
	public ResponseEntity<?> removeFromProjectsFiltered(){
		service.removeFromProjectsFiltered();
		return ResponseEntity.ok("ok");
	}

	@DeleteMapping("/all")
	public ResponseEntity<?> removeAll(){
		service.removeAll();
		return ResponseEntity.ok("Ok");
	}

	@PostMapping("/save-git-repository-version")
	public ResponseEntity<?> saveGitRepositoryTruckFactor(@RequestBody String repositoryPath) throws Exception{
		return ResponseEntity.ok(service.saveGitRepositoryVersion(repositoryPath));
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getGitRepositoryVersionById(@PathVariable("id") Long id) throws Exception{
		return ResponseEntity.ok(service.getGitRepositoryVersionById(id));
	}
}
