package br.com.gitanalyzer.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.gitanalyzer.model.entity.Commit;
import br.com.gitanalyzer.model.entity.Contributor;

public class ContributorUtils {

	public List<Contributor> setActiveContributors(List<Contributor> contributors, List<Commit> commits){
		if(contributors != null && contributors.size() > 0) {
			Date currentDate = commits.get(0).getAuthorDate();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentDate);
			calendar.add(Calendar.YEAR, -1);
			Date date = calendar.getTime();
			commits = commits.stream().filter(c -> c.getAuthorDate().after(date)).toList();
			contributorFor: for (Contributor contributor : contributors) {
				for (Commit commit : commits) {
					if (commit.getAuthor().equals(contributor)) {
						contributor.setActive(true);
						continue contributorFor;
					}
				}
			}
		}
		return contributors;
	}
}
