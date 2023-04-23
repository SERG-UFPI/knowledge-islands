package br.com.gitanalyzer.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.gitanalyzer.extractors.CommitExtractor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommitService {

	public void generateFileLists(String path) throws URISyntaxException, IOException, InterruptedException {
		log.info("Generating linguist file");
		String pathRubyScript = CommitService.class.getResource("/linguist.rb").toURI().getPath();
		String command = "ruby "+pathRubyScript+" "+path;
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();
		BufferedReader processIn = new BufferedReader(
				new InputStreamReader(process.getInputStream()));
		String line;

		File file = new File(path+"/linguistfiles.log");
		FileOutputStream fos = new FileOutputStream(file);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		while ((line = processIn.readLine()) != null) {
			bw.write(line);
			bw.newLine();
		} 
		bw.close();
		processIn.close();
		log.info("End generation linguist file");
	}

	public void generateCommitFile(String path) throws URISyntaxException, IOException, InterruptedException {
		log.info("Generating commit file");
		String pathCommitScript = CommitService.class.getResource("/commit_log_script.sh").toURI().getPath();
		String command = "sh "+pathCommitScript+" "+path;
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();
		log.info("End generation commit file");
	}

	public void generateCommitFileFile(String projectPath) throws URISyntaxException, IOException, InterruptedException {

		log.info("Generating commitFile file");
		CommitExtractor commitExtractor = new CommitExtractor();
		commitExtractor.generateCommitFileFile(projectPath);
		log.info("End generation commitFile file");
	}
}
