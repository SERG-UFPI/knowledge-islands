package br.com.gitanalyzer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.main.dto.DownloaderForm;
import br.com.gitanalyzer.service.DownloaderService;

@RestController
@RequestMapping("/downloader")
public class DownloaderController {

	@Autowired
	private DownloaderService service;

	@PostMapping()
	public ResponseEntity<?> download(@RequestBody DownloaderForm form){
		service.download(form);
		return ResponseEntity.status(HttpStatus.CREATED).body("Download finished");
	}

}
