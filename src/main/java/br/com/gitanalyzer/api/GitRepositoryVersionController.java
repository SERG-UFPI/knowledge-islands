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
	public ResponseEntity<String> remove(@PathVariable Long id){
		service.remove(id);
		return ResponseEntity.ok("Ok");
	}

	@DeleteMapping("/project/{id}")
	public ResponseEntity<?> removeFromProject(@PathVariable Long id){
		service.removeFromProject(id);
		return ResponseEntity.ok("ok");
	}

	@PostMapping("/remove-from-repos-filtered")
	public ResponseEntity<String> removeFromProjectsFiltered(){
		service.removeFromProjectsFiltered();
		return ResponseEntity.ok("ok");
	}

	@DeleteMapping("/all")
	public ResponseEntity<String> removeAll(){
		service.removeAll();
		return ResponseEntity.ok("Ok");
	}
	
	@PostMapping("/save-git-repository-version-not-filtered")
	public ResponseEntity<?> saveGitRepositoryVersionNotFiltered() throws Exception{
		service.saveGitRepositoryVersionNotFiltered();
		return ResponseEntity.ok("Finished");
	}

	@PostMapping("/save-git-repository-version")
	public ResponseEntity<?> saveGitRepositoryTruckFactor(@RequestBody String repositoryPath) throws Exception{
		return ResponseEntity.ok(service.saveGitRepositoryAndGitRepositoryVersion(repositoryPath));
	}

	@PostMapping("/save-git-repository-version-genai")
	public ResponseEntity<String> saveGitRepositoryVersionGenai(@RequestBody String repositoryPath) throws Exception{
		service.saveGitRepositoryVersionGenai(repositoryPath);
		return ResponseEntity.ok("Finished");
	}

	@PostMapping("/save-git-repositories-version-shared-link")
	public ResponseEntity<String> saveGitRepositoriesVersionSharedLink() throws Exception{
		service.saveGitRepositoriesVersionSharedLink();
		return ResponseEntity.ok("Finished");
	}

	@PostMapping("/save-git-repositories-version-shared-link-genAi")
	public ResponseEntity<String> saveGitRepositoriesVersionGenAiSharedLink() throws Exception{
		service.saveGitRepositoriesVersionSharedLinkGenAi();
		return ResponseEntity.ok("Finished");
	}
	
	@PostMapping("/save-shared-link-commits-versions")
	public ResponseEntity<?> saveSharedLinkCommitsVersions() throws Exception{
		service.saveSharedLinkCommitsVersions();
		return ResponseEntity.ok("Finished");
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getGitRepositoryVersionById(@PathVariable("id") Long id) throws Exception{
		return ResponseEntity.ok(service.getGitRepositoryVersionById(id));
	}

}
