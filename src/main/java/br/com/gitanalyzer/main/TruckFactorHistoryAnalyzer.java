package br.com.gitanalyzer.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.enums.KnowledgeMetric;
import br.com.gitanalyzer.extractors.CommitExtractor;
import br.com.gitanalyzer.model.Commit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TruckFactorHistoryAnalyzer{

	int numberAllDevs, numberAnalysedDevs, numberAnalysedDevsAlias, 
	numberAllFiles, numberAnalysedFiles, numberAllCommits, numberAnalysedCommits, truckfactor;
	String projectName;

	public void directoriesTruckFactorHistoryAnalyzes(String pathToDirectories) throws IOException, 
	NoHeadException, GitAPIException{
		java.io.File dir = new java.io.File(pathToDirectories);
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				analyzer(pathToDirectories, projectPath, KnowledgeMetric.DOE);
			}
		}
	}

	private void analyzer(String pathToDirectories, String projectPath, KnowledgeMetric knowledgeMetric)  {
		TruckFactorAnalyzer factorAnalyzer = new TruckFactorAnalyzer(); 
		TruckFactorHistoryAnalyzer analyzer = new TruckFactorHistoryAnalyzer();
		try {
			Git git = Git.open(new File(projectPath));
			String[] hashes = analyzer.extractListOfHashes(projectPath);
			for (String hash : hashes) {
				git.checkout().setName(hash).call();
				analyzer.executeLinguisticScript(pathToDirectories, projectPath);
				analyzer.executeClocScript(pathToDirectories, projectPath);
				//				analyzer.executeMainScript(pathToDirectories);
				factorAnalyzer.projectTruckFactorAnalyzes(projectPath, knowledgeMetric);
			}
			git.checkout().setName("master").call();
		} catch (GitAPIException | IOException | InterruptedException e) {
			log.error(e.getMessage());
		}
	}

	private void executeClocScript(String pathToDirectories, String path) throws IOException, InterruptedException{
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("bash", "-c", "sh "+pathToDirectories+"cloc_script.sh "+path);
		Process p = pb.start();
		p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream())); 
		String line; 
		while((line = reader.readLine()) != null) { 
			System.out.println(line);
		}
	}

	private void executeLinguisticScript(String pathToDirectories, String path) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("bash", "-c", "ruby "+pathToDirectories+"linguist.rb "+path);
		pb.redirectOutput(new File(path+"linguistfiles.log"));
		Process p = pb.start();
		p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream())); 
		String line; 
		while((line = reader.readLine()) != null) { 
			System.out.println(line);
		}
	}

	private String[] extractListOfHashes(String projectPath) throws NoHeadException, GitAPIException {
		CommitExtractor commitExtractor = new CommitExtractor();
		List<Commit> commits = commitExtractor.getCommitsDatesAndHashes(projectPath);
		String[] hashes = new String[5];
		int index = 0;
		Date date = commits.get(0).getDate();
		hashes[index] = commits.get(0).getExternalId();
		index++;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, -1);
		date = calendar.getTime();
		for (Commit commit: commits) {
			if (commit.getDate().before(date)) {
				date = commit.getDate();
				hashes[index] = commit.getExternalId();
				calendar = Calendar.getInstance();
				calendar.setTime(date);
				calendar.add(Calendar.YEAR, -1);
				date = calendar.getTime();
				index++;
				if (index > hashes.length - 1) {
					break;
				}
			}
		}
		return hashes;
	}
}
