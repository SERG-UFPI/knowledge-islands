package br.com.gitanalyzer.api;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.CkMeasuresService;

@RestController
@RequestMapping("/api/ck-measures")
public class CkMeasuresController {
	
	@Autowired
	private CkMeasuresService service;
	
	@PostMapping
	public ResponseEntity<?> extractMeasure(@RequestBody String path) throws IOException{
		service.extractMeasures(path);
		return ResponseEntity.ok(null);
	}
	
	@PostMapping("set-quality-last-version")
	public ResponseEntity<?> setQualityLastVersion(@RequestBody String path) throws IOException{
		service.setQualityLastVersion(path);
		return ResponseEntity.ok(null);
	}
	
}
