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
	private static String shellFilePath = "/home/otavio/projetosHistorico/main.sh";

	public static void main(String[] args)  {

		String pathToDir = args[0];
		TruckFactorHistoryAnalyzer analyzer = new TruckFactorHistoryAnalyzer();
		Git git;
		Repository repository;
		try {
			git = Git.open(new File(projectTest));
			repository = git.getRepository();
			String[] hashes = analyzer.extractListOfHashes(git);
			for (String hash : hashes) {
				git.checkout().setName(hash).call();
				StringBuilder output = new StringBuilder();
				ProcessBuilder pb = new ProcessBuilder(shellFilePath);
				Process p = pb.start();
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while((line = br.readLine()) != null) {
					output.append(line + "\n");
				}
				int exitVal = p.waitFor();
				if (exitVal == 0) {
					System.out.println(output);
				}
				//analyzer.executeTruckFactorAnalyzes(pathToDir);
				System.out.println();
			}
			git.checkout().setName("master").call();
		} catch (GitAPIException | InterruptedException | IOException e) {
			e.printStackTrace();
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
		calendar.add(Calendar.MONTH, -3);
		date = calendar.getTime();
		for (RevCommit commit: commitsList) {
			if (commit.getAuthorIdent().getWhen().before(date)) {
				date = commit.getAuthorIdent().getWhen();
				hashes[index] = commit.getName();
				calendar = Calendar.getInstance();
				calendar.setTime(date);
				calendar.add(Calendar.MONTH, -3);
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
