package br.com.gitanalyzer.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.ProjectVersionService;

@RestController
@RequestMapping("/api/project-version")
public class ProjectVersionController {

	@Autowired
	private ProjectVersionService service;

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
	
	@PostMapping("/remove-from-projects-filtered")
	public ResponseEntity<?> removeFromProjectsFiltered(){
		service.removeFromProjectsFiltered();
		return ResponseEntity.ok("ok");
	}
	
	@DeleteMapping("/all")
	public ResponseEntity<?> removeAll(){
		service.removeAll();
		return ResponseEntity.ok("Ok");
	}
}
