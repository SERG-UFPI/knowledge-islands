package br.com.gitanalyzer.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.com.gitanalyzer.model.Commit;

public class CommitUtils {
	
	public static void sortCommitsByDate(List<Commit> commits) {
		Collections.sort(commits, new Comparator<Commit>() {
			@Override
			public int compare(Commit c1, Commit c2) {
				return c2.getDate().compareTo(c1.getDate());
			}
		});
	}
	
}
