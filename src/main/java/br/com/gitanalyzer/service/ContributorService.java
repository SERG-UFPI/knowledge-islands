package br.com.gitanalyzer.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.model.entity.Commit;
import br.com.gitanalyzer.model.entity.Contributor;

@Service
public class ContributorService {

	public List<Contributor> getContributorFromCommits(List<Commit> commits){
		List<Contributor> contributors = new ArrayList<Contributor>();
		forCommit: for (Commit commit : commits) {
			Contributor contributor = commit.getAuthor();
			for (Contributor contributor2 : contributors) {
				if (contributor2.equals(contributor)) {
					continue forCommit;
				}
			}
			contributors.add(contributor);
		}
		return contributors;
	}

	public List<Contributor> setAlias(List<Contributor> contributors, String projectName){
		List<Contributor> contributorsAliases = new ArrayList<>();
		forContributors:for (int i = 0; i < contributors.size(); i++) {
			for (Contributor contributorAlias : contributorsAliases) {
				if(contributorAlias.getAlias() != null && !contributorAlias.getAlias().isEmpty()) {
					for (Contributor contributorAliasAux : contributorAlias.getAlias()) {
						if (contributors.get(i).equals(contributorAliasAux)) {
							continue forContributors;
						}
					}
				}
			}
			for(int j = i+1; j < contributors.size(); j++) {
				boolean alias = checkAliasContributors(contributors.get(j), contributors.get(i));
				if(alias == true) {
					if(contributors.get(i).getAlias() == null) {
						contributors.get(i).setAlias(new HashSet<>());
					}
					contributors.get(i).getAlias().add(contributors.get(j));
				}
			}
			if(contributors.get(i).getAlias() != null && !contributors.get(i).getAlias().isEmpty()) {
				for (Contributor alias : contributors.get(i).getAlias()) {
					for (Contributor contributorAlias : contributorsAliases) {
						if(contributorAlias.getAlias() != null && !contributorAlias.getAlias().isEmpty()) {
							for (Contributor contributorAliasAux : contributorAlias.getAlias()) {
								if (alias.equals(contributorAliasAux)) {
									List<Contributor> aux = new ArrayList<>();
									aux.addAll(contributors.get(i).getAlias());
									aux.add(contributors.get(i));
									contributors.get(i).getAlias().clear();
									contributorAlias.getAlias().addAll(aux);
									continue forContributors;
								}
							}
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
			if(contributor1.getName() != null) {
				int distance = StringUtils.getLevenshteinDistance(contributor2.getName().toUpperCase()
						, contributor1.getName().toUpperCase());
				if (distance <= 1) {
					return true;
				}
			}
		}
		return false;
	}
}
