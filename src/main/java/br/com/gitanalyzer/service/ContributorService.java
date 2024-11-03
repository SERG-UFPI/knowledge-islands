package br.com.gitanalyzer.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.model.entity.Commit;
import br.com.gitanalyzer.model.entity.Contributor;

@Service
public class ContributorService {

	public List<Contributor> setActiveContributors(List<Contributor> contributors, List<Commit> commits){
		if(contributors != null && !contributors.isEmpty()) {
			Date currentDate = commits.get(0).getAuthorDate();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentDate);
			calendar.add(Calendar.YEAR, -1);
			Date date = calendar.getTime();
			commits = commits.stream().filter(c -> c.getAuthorDate().after(date)).toList();
			contributorFor: for (Contributor contributor : contributors) {
				for (Commit commit : commits) {
					if (commit.getAuthor().equals(contributor)) {
						//contributor.setActive(true);
						continue contributorFor;
					}
				}
			}
		}
		return contributors;
	}

	public List<Contributor> getContributorFromCommits(List<Commit> commits){
		List<Contributor> contributors = new ArrayList<>();
		forCommit: for (Commit commit : commits) {
			Contributor contributor = commit.getAuthor();
			if(contributors.stream().anyMatch(c -> c.equals(contributor))) {
				continue forCommit;
			}
			contributors.add(contributor);
		}
		return contributors;
	}

	public List<Contributor> setAlias(List<Contributor> contributors, String projectName){
		List<Contributor> contributorsAliases = new ArrayList<>();
		for (Contributor contributor : contributors) {
			if(contributor.getId() != null) {
				contributorsAliases.add(contributor);
			}
		}
		forContributors:for (int i = 0; i < contributors.size(); i++) {
			//check if contributor i is present in the aliases of the already added contributorsAliases
			for (Contributor contributorAlias : contributorsAliases) {
				if(contributors.get(i).equals(contributorAlias)) {
					continue forContributors;
				}else if(contributorAlias.getAlias() != null && !contributorAlias.getAlias().isEmpty()) {
					for (Contributor contributorAliasAux : contributorAlias.getAlias()) {
						if (contributors.get(i).equals(contributorAliasAux)) {
							continue forContributors;
						}
					}
				}
			}
			//set all alias of contributor i
			forContributor2:for(int j = i+1; j < contributors.size(); j++) {
				if(checkAliasContributors(contributors.get(j), contributors.get(i))) {
					if(contributors.get(i).getAlias() == null) {
						contributors.get(i).setAlias(new HashSet<>());
					}
					for(Contributor alias: contributors.get(i).getAlias()) {
						if(alias.equals(contributors.get(j))) {
							continue forContributor2;
						}
					}
					contributors.get(i).getAlias().add(contributors.get(j));
				}
			}
			for (Contributor contributorAlias : contributorsAliases) {
				if(contributorAlias.getAlias() != null && !contributorAlias.getAlias().isEmpty()) {
					for (Contributor contributorAliasAux : contributorAlias.getAlias()) {
						if(checkAliasContributors(contributorAliasAux, contributors.get(i)) || 
								(contributors.get(i).getAlias() != null && contributors.get(i).getAlias().stream().anyMatch(c -> checkAliasContributors(contributorAliasAux, c)))) {
							List<Contributor> aux = new ArrayList<>();
							aux.add(contributors.get(i));
							if(contributors.get(i).getAlias() != null) {
								aux.addAll(contributors.get(i).getAlias());
								contributors.get(i).getAlias().clear();
							}
							contributorAlias.getAlias().addAll(aux);
							continue forContributors;
						}
					}
				}
			}
			contributorsAliases.add(contributors.get(i));
		}
		return contributorsAliases;
	}

	public boolean checkAliasContributors(Contributor contributor1, Contributor contributor2) {
		if(contributor1.getEmail().equals(contributor2.getEmail())) {
			return true;
		}else {
			int distance = StringUtils.getLevenshteinDistance(contributor2.getName().toUpperCase()
					, contributor1.getName().toUpperCase());
			if (distance <= 1) {
				return true;
			}
		}
		return false;
	}
}
