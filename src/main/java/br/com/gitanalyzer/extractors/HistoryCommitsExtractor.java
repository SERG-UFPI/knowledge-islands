package br.com.gitanalyzer.extractors;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import br.com.gitanalyzer.dto.form.HistoryReposTruckFactorForm;
import br.com.gitanalyzer.enums.TimeIntervalTypeEnum;
import br.com.gitanalyzer.model.Commit;

public class HistoryCommitsExtractor {

	public String[] saveCommitsHashs(String path, int numberYears) {
		String[] hashes = getCommitHashesYears(path, numberYears);
		String fullPath = path+"commitsHistory.log";
		FileWriter writer = null;
		try {
			writer = new FileWriter(fullPath);
			PrintWriter printWriter = new PrintWriter(writer);
			for(String hash: hashes) {
				printWriter.println(hash);
			}
			printWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hashes;
	}

	public String[] getCommitHashesYears(String path, int numberYears) {
		CommitExtractor commitExtractor = new CommitExtractor();
		List<Commit> commits = commitExtractor.getCommitsDatesAndHashes(path);
		Collections.sort(commits, new Comparator<Commit>() {
			@Override
			public int compare(Commit c1, Commit c2) {
				return c2.getAuthorDate().compareTo(c1.getAuthorDate());
			}
		});
		String[] hashes = new String[numberYears];
		int index = 0;
		Date date = commits.get(0).getAuthorDate();
		hashes[index] = commits.get(0).getSha();
		index++;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, -1);
		date = calendar.getTime();
		for (Commit commit: commits) {
			if (commit.getAuthorDate().before(date)) {
				date = commit.getAuthorDate();
				hashes[index] = commit.getSha();
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

	public List<String> getCommitHashesByInterval(HistoryReposTruckFactorForm form) {
		CommitExtractor commitExtractor = new CommitExtractor();
		List<Commit> commits = commitExtractor.getCommitsDatesAndHashes(form.getPath());
		Collections.sort(commits, new Comparator<Commit>() {
			@Override
			public int compare(Commit c1, Commit c2) {
				return c2.getAuthorDate().compareTo(c1.getAuthorDate());
			}
		});
		Collections.reverse(commits);
		List<String> hashes = new ArrayList<String>();
		Date date = commits.get(0).getAuthorDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int calendarType = form.getIntervalType().equals(TimeIntervalTypeEnum.MONTH)?Calendar.MONTH:Calendar.YEAR;
		calendar.add(calendarType, form.getInterval());
		date = calendar.getTime();
		for (Commit commit: commits) {
			if (commit.getAuthorDate().after(date)) {
				hashes.add(commit.getSha());
				date = commit.getAuthorDate();
				calendar = Calendar.getInstance();
				calendar.setTime(date);
				calendar.add(calendarType, form.getInterval());
				date = calendar.getTime();
			}
		}
		hashes.add(commits.get(commits.size()-1).getSha());
		return hashes.stream().distinct().toList();
	}

	public int getNumberOfYearsFromFolderProjects() {

		return 0;
	}

}
