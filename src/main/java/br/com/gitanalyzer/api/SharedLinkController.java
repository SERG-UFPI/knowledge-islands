package br.com.gitanalyzer.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.SharedLinkService;

@RestController
@RequestMapping("/api/shared-link")
public class SharedLinkController {

	@Autowired
	private SharedLinkService service;

	@PostMapping("/create-file-shared-links")
	public ResponseEntity<?> createFileSharedLinks() throws Exception{
		return ResponseEntity.ok(service.getFileSharedLink());
	}
}
