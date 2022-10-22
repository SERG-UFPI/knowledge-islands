package br.com.gitanalyzer.main;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.gitanalyzer.extractors.CommitExtractor;
import br.com.gitanalyzer.model.Commit;

public class ExtractHashesCommits {

	public static void main(String[] args) {
		CommitExtractor commitExtractor = new CommitExtractor();
		List<Commit> commits = commitExtractor.getCommitsDatesAndHashes(args[0]);
		String[] hashes = new String[7];
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
		for (String hash : hashes) {
			System.out.println(hash);
		}
	}

}
