package br.com.knowledgeislands.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledgeislands.service.FileService;

@RestController
@RequestMapping("/api/file")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class FileController {

	@Autowired
	private FileService service;

	@PostMapping("/fix-chinese-paths")
	public ResponseEntity<?> fixChinesePaths() {
		service.fixChinesePaths();
		return ResponseEntity.status(HttpStatus.CREATED).body("Finished");
	}

}
