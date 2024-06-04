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

import br.com.gitanalyzer.dto.form.CloneRepoForm;
import br.com.gitanalyzer.model.entity.GitRepositoryVersionProcess;
import br.com.gitanalyzer.service.GitRepositoryVersionProcessService;

@RestController
@RequestMapping("/api/git-repository-version-process")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class GitRepositoryVersionProcessController {
	@Autowired
	private GitRepositoryVersionProcessService service;

	@GetMapping("/user/{id}")
	public ResponseEntity<?> getProcessesByUserId(@PathVariable("id") Long id) throws Exception{
		return ResponseEntity.ok(service.getByUserId(id));
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getProcessesById(@PathVariable("id") Long id) throws Exception{
		return ResponseEntity.ok(service.getProcessesById(id));
	}

	@PostMapping("start-git-repository-version-process")
	public ResponseEntity<GitRepositoryVersionProcess> cloneAndSaveGitRepositoryTruckFactor(@RequestBody CloneRepoForm form) throws Exception{
		return ResponseEntity.ok(service.cloneAndSaveGitRepositoryTruckFactor(form));
	}
	
	@DeleteMapping("/all")
	public ResponseEntity<?> removeAll(){
		service.removeAll();
		return ResponseEntity.ok("Ok");
	}
}
