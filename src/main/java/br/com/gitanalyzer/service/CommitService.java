package br.com.gitanalyzer.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.stereotype.Service;

import br.com.gitanalyzer.extractors.CommitExtractor;
import br.com.gitanalyzer.utils.ProjectUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommitService {

	private ProjectUtils projectUtils = new ProjectUtils();

	public void generateCommitFile(String path) throws URISyntaxException, IOException, InterruptedException {
		String name = projectUtils.extractProjectName(path);
		log.info("Generating commit file of "+name);
		String pathCommitScript = CommitService.class.getResource("/commit_log_script.sh").toURI().getPath();
		String command = "sh "+pathCommitScript+" "+path;
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();
		log.info("End generation commit file");
	}

	public void generateCommitFileFile(String projectPath) throws URISyntaxException, IOException, InterruptedException {
		String name = projectUtils.extractProjectName(projectPath);
		log.info("Generating commitFile file of "+name);
		CommitExtractor commitExtractor = new CommitExtractor();
		commitExtractor.generateCommitFileFile(projectPath);
		log.info("End generation commitFile file");
	}

}