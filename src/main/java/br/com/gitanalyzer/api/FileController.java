package br.com.gitanalyzer.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.FileService;

@RestController
@RequestMapping("/api/downloader")
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
