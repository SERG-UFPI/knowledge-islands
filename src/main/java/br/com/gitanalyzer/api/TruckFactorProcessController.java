package br.com.gitanalyzer.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.TruckFactorProcessService;

@RestController
@RequestMapping("/api/truck-factor-process")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class TruckFactorProcessController {

	@Autowired
	private TruckFactorProcessService service;

	@GetMapping("/{id}")
	public ResponseEntity<?> getProcessesByUserId(@PathVariable("id") Long id) throws Exception{
		return ResponseEntity.ok(service.getByUserId(id));
	}
}
