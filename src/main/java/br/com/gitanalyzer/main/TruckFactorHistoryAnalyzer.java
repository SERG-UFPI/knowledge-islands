package br.com.gitanalyzer.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.RevFilter;

public class TruckFactorHistoryAnalyzer extends TruckFactorAnalyzer{

	int numberAllDevs, numberAnalysedDevs, numberAnalysedDevsAlias, 
	numberAllFiles, numberAnalysedFiles, numberAllCommits, numberAnalysedCommits, truckfactor;
	String projectName;

	private static String projectTest = "/home/otavio/projetosHistorico/WordPress/";
	private static String projectsFolder = "/home/otavio/projetosHistorico/";

	public static void main(String[] args)  {

		TruckFactorHistoryAnalyzer analyzer = new TruckFactorHistoryAnalyzer();
		Git git;
		Repository repository;
		try {
			git = Git.open(new File(projectTest));
			repository = git.getRepository();
			String[] hashes = analyzer.extractListOfHashes(git);
			for (String hash : hashes) {
				git.checkout().setName(hash).call();
				analyzer.executeLinguisticScript(projectTest);
				analyzer.executeClocScript(projectTest);
				//analyzer.executeTruckFactorAnalyzes(pathToDir);
			}
			git.checkout().setName("master").call();
		} catch (GitAPIException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void executeClocScript(String path) throws IOException, InterruptedException{
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("bash", "-c", "sh "+projectsFolder+"cloc_script.sh "+path);
		Process p = pb.start();
		p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream())); 
		String line; 
		while((line = reader.readLine()) != null) { 
			System.out.println(line);
		}
	}

	private void executeLinguisticScript(String path) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("bash", "-c", "ruby "+projectsFolder+"linguist.rb "+path);
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

	private String[] extractListOfHashes(Git git) throws NoHeadException, GitAPIException {
		String[] hashes = new String[5];
		Iterable<RevCommit> commitsIterable = git.log().setRevFilter(RevFilter.NO_MERGES).call();
		List<RevCommit> commitsList = new ArrayList<RevCommit>();
		commitsIterable.forEach(commitsList::add);
		Collections.sort(commitsList, new Comparator<RevCommit>() {
			public int compare(RevCommit commit1, RevCommit commit2) {
				if (commit1.getAuthorIdent().getWhen().after(commit2.getAuthorIdent().getWhen())) {
					return -1;
				}else if(commit1.getAuthorIdent().getWhen().before(commit2.getAuthorIdent().getWhen())) {
					return 1;
				}else {
					return 0;
				}
			}
		});
		int index = 0;
		Date date = commitsList.get(0).getAuthorIdent().getWhen();
		hashes[index] = commitsList.get(0).getName();
		index++;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, -1);
		date = calendar.getTime();
		for (RevCommit commit: commitsList) {
			if (commit.getAuthorIdent().getWhen().before(date)) {
				date = commit.getAuthorIdent().getWhen();
				hashes[index] = commit.getName();
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
