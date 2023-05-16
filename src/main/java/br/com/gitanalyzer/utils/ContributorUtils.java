package br.com.gitanalyzer.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.gitanalyzer.model.Commit;
import br.com.gitanalyzer.model.Contributor;

public class ContributorUtils {

	public List<Contributor> getActiveContributors(List<Contributor> contributors, List<Commit> commits){
		List<Contributor> activeContributors = new ArrayList<Contributor>();
		Date currentDate = commits.get(0).getDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, -1);
		Date date = calendar.getTime();
		commits = commits.stream().filter(c -> c.getDate().after(date)).toList();
		contributorFor: for (Contributor contributor : contributors) {
			for (Commit commit : commits) {
				if (commit.getAuthor().equals(contributor)) {
					activeContributors.add(contributor);
					continue contributorFor;
				}
			}
		}
		return activeContributors;
	}
}
