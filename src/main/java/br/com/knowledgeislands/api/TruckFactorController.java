package br.com.knowledgeislands.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledgeislands.dto.form.HistoryReposTruckFactorForm;
import br.com.knowledgeislands.dto.form.RepositoryKnowledgeMetricForm;
import br.com.knowledgeislands.model.entity.TruckFactor;
import br.com.knowledgeislands.service.TruckFactorService;

@RestController
@RequestMapping("/api/truck-factor")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class TruckFactorController {

	@Autowired
	private TruckFactorService service;

	@GetMapping("/{id}")
	public ResponseEntity<?> getTruckFactorById(@PathVariable Long id) throws Exception{
		return ResponseEntity.ok(service.getTruckFactorById(id));
	}

	@PostMapping("repo-truck-factor-folder")
	public ResponseEntity<?> directoriesTruckFactorAnalyzes(@RequestBody RepositoryKnowledgeMetricForm form){
		try {
			service.directoriesTruckFactorAnalyzes(form);
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.OK).body("Analysis finished");
	}

	@PostMapping("save-truck-factor")
	public ResponseEntity<TruckFactor> saveTruckFactor(@RequestBody Long idGitRepositoryVersionKnowledgeModel){
		try {
			return ResponseEntity.ok(service.saveTruckFactor(idGitRepositoryVersionKnowledgeModel));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("save-full-truck-factor")
	public ResponseEntity<List<TruckFactor>> generateTruckFactorRepository(@RequestBody RepositoryKnowledgeMetricForm form){
		try {
			return ResponseEntity.ok(service.generateTruckFactorRepository(form));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@PostMapping("/save-all-truck-factor")
	public ResponseEntity<String> saveAllTruckFactor(){
		service.saveAllTruckFactor();
		return ResponseEntity.ok("Finished");
	}
	
	@PostMapping("/save-all-truck-factor-is-null")
	public ResponseEntity<String> saveAllTruckFactorIsNull(){
		service.saveAllTruckFactorIsNull();
		return ResponseEntity.ok("Finished");
	}

	@PostMapping("generate-logs-repo-truck-factor")
	public ResponseEntity<List<TruckFactor>> generateLogsTruckFactorRepository(@RequestBody RepositoryKnowledgeMetricForm form){
		try {
			return ResponseEntity.ok(service.generateLogsTruckFactorRepository(form));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("history-truck-factor-folder")
	public ResponseEntity<?> historyReposTruckFactor(@RequestBody HistoryReposTruckFactorForm form) throws URISyntaxException, InterruptedException{
		try {
			service.historyReposTruckFactor(form);
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.OK).body("Analysis finished");
	}

	@PostMapping("history-truck-factor")
	public ResponseEntity<?> historyRepoTruckFactor(@RequestBody HistoryReposTruckFactorForm form) throws URISyntaxException, InterruptedException{
		try {
			service.historyRepoTruckFactor(form);
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.OK).body("Analysis finished");
	}
	
	@DeleteMapping("/all")
	public ResponseEntity<String> removeAll(){
		service.removeAll();
		return ResponseEntity.ok("Ok");
	}
}
