package br.com.gitanalyzer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import br.com.gitanalyzer.dto.form.CloneRepoForm;

@SpringBootTest
@TestPropertySource("/application-test.properties")
public class DownloaderServiceTest {

	@Value("${configuration.clone.path}")
	private String cloneFolder;
	@Autowired
	private DownloaderService service;

	@Test
	public void cloneProject_checkIfFolderExists() throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		CloneRepoForm form = CloneRepoForm.builder()
				.cloneUrl("https://github.com/OtavioCury/git-analyzer.git").build();
		String folderPath = cloneFolder+"git-analyzer"+"/";
		assertEquals(service.cloneProject(form), folderPath);
	}
}
