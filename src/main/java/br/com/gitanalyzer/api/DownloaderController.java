package br.com.gitanalyzer.api;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.dto.form.CloneRepoForm;
import br.com.gitanalyzer.dto.form.DownloaderPerLanguageForm;
import br.com.gitanalyzer.dto.form.DownloaderPerOrgForm;
import br.com.gitanalyzer.service.DownloaderService;

@RestController
@RequestMapping("/api/downloader")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class DownloaderController {

	@Autowired
	private DownloaderService service;

	@PostMapping("/donwload-per-language")
	public ResponseEntity<?> downloadPerLanguage(@RequestBody DownloaderPerLanguageForm form) throws URISyntaxException, InterruptedException{
		service.downloadPerLanguage(form);
		return ResponseEntity.status(HttpStatus.CREATED).body("Download finished");
	}

	@PostMapping("/donwload-per-org")
	public ResponseEntity<?> downloadPerorg(@RequestBody DownloaderPerOrgForm form) throws URISyntaxException, InterruptedException{
		service.downloadPerOrg(form);
		return ResponseEntity.status(HttpStatus.CREATED).body("Download finished");
	}

	@PostMapping("clone-repository")
	public ResponseEntity<?> cloneProject(@RequestBody CloneRepoForm form) {
		return ResponseEntity.ok(service.cloneProject(form));
	}

	@PostMapping("clone-shared-links-repositories")
	public ResponseEntity<?> cloneSharedLinkRepositories() throws URISyntaxException, IOException, InterruptedException{
		return ResponseEntity.ok(service.cloneRepositoriesSharedLinks());
	}

	@PostMapping("clone-shared-links-repositories-generate-logs")
	public ResponseEntity<?> cloneRepositoriesSharedLinksGenerateLogs() throws URISyntaxException, IOException, InterruptedException{
		service.cloneRepositoriesSharedLinksGenerateLogs();
		return ResponseEntity.status(HttpStatus.CREATED).body("Download finished");
	}

}
