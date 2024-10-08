package br.com.gitanalyzer.api;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.exceptions.NoCommitForFileException;
import br.com.gitanalyzer.service.GitRepositoryFileService;

@RestController
@RequestMapping("/api/git-repository-file")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class GitRepositoryFileController {
	@Autowired
	private GitRepositoryFileService service;

	@PostMapping("/set-commits-of-files")
	public ResponseEntity<?> setCommitsOfFiles() throws NoHeadException, IOException, GitAPIException, NoCommitForFileException{
		return ResponseEntity.ok(service.setCommitsOfFilesSharedLinks());
	}
}
