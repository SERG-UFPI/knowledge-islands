package br.com.gitanalyzer.controllers;

import java.net.URISyntaxException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.main.dto.CloneRepoForm;
import br.com.gitanalyzer.main.dto.DownloaderForm;
import br.com.gitanalyzer.service.DownloaderService;

@RestController
@RequestMapping("/api/downloader")
public class DownloaderController {

	@Autowired
	private DownloaderService service;

	@PostMapping
	public ResponseEntity<?> download(@RequestBody DownloaderForm form) throws URISyntaxException, InterruptedException{
		service.download(form);
		return ResponseEntity.status(HttpStatus.CREATED).body("Download finished");
	}

	@PostMapping("clone-repository")
	public ResponseEntity<?> cloneProject(@RequestBody CloneRepoForm form) throws InvalidRemoteException, TransportException, GitAPIException{
		return ResponseEntity.ok(service.cloneProject(form));
	}

}
