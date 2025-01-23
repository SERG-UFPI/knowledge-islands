package br.com.gitanalyzer.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.model.entity.Commit;
import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.repository.ContributorRepository;
import br.com.gitanalyzer.utils.KnowledgeIslandsUtils;

@Service
public class ContributorService {

	@Autowired
	private EmailService emailService;
	@Autowired
	private ContributorRepository contributorRepository;

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
		for (Commit commit : commits) {
			Contributor contributor = commit.getAuthor();
			if(contributors.stream().anyMatch(c -> c.equals(contributor))) {
				continue;
			}
			contributors.add(contributor);
		}
		return contributors;
	}

	public List<Contributor> setAlias(List<Contributor> contributors){
		List<Contributor> finalContributors = new ArrayList<>();
		for (Contributor contributor : contributors) {
			if(contributor.getId() != null) {
				finalContributors.add(contributor);
			}
		}
		forContributor:for (int i = 0; i < contributors.size(); i++) {
			for (Contributor contributorFinal : finalContributors) {
				List<Contributor> aliasesFinal = contributorFinal.contributorAlias();
				for (Contributor contributorAliasFinal : aliasesFinal) {
					if(contributorAliasFinal.equals(contributors.get(i))) {
						continue forContributor;
					}
				}
			}
			for(int j = i+1; j < contributors.size(); j++) {
				if(checkAliasContributors(contributors.get(i), contributors.get(j))) {
					if(contributors.get(i).getAlias() == null) {
						contributors.get(i).setAlias(new HashSet<>());
					}
					contributors.get(i).getAlias().add(contributors.get(j));
				}
			}
			List<Contributor> contributorAliases = contributors.get(i).contributorAlias();
			for (Contributor contributorFinal : finalContributors) {
				List<Contributor> aliasesFinal = contributorFinal.contributorAlias();
				boolean isAlias = false;
				for (Contributor contributorAliasFinal : aliasesFinal) {
					if(contributorAliases.stream().anyMatch(c -> checkAliasContributors(contributorAliasFinal, c))) {
						isAlias = true;
						break;
					}
				}
				if(isAlias) {
					if(contributorFinal.getAlias() == null) {
						contributorFinal.setAlias(new HashSet<>());
					}
					for (Contributor contributorAlias : contributorAliases) {
						if(contributorFinal.getAlias().stream().noneMatch(c -> c.equals(contributorAlias))) {
							contributorAlias.clearAlias();
							contributorFinal.getAlias().add(contributorAlias);
						}
					}
					continue forContributor;
				}
			}
			finalContributors.add(contributors.get(i));
		}
		List<Contributor> contributorRemoved = new ArrayList<>();
		for (int i = 0; i < finalContributors.size(); i++) {
			List<Contributor> alias1 = finalContributors.get(i).contributorAlias();
			for (int j = i+1; j < finalContributors.size(); j++) {
				boolean isEqual = false;
				List<Contributor> alias2 = finalContributors.get(j).contributorAlias();
				for (Contributor contributor : alias1) {
					if(alias2.stream().anyMatch(a -> a.equals(contributor))) {
						isEqual = true;
						break;
					}
				}
				if(isEqual) {
					for (Contributor contributor : alias2) {
						if(alias1.stream().noneMatch(a -> a.equals(contributor))) {
							if (finalContributors.get(i).getAlias() == null) {
								finalContributors.get(i).setAlias(new HashSet<>());
							}
							contributor.clearAlias();
							finalContributors.get(i).getAlias().add(contributor);
						}
					}
					contributorRemoved.add(finalContributors.get(j));
				}
			}
		}
		finalContributors.removeAll(contributorRemoved);
		return finalContributors;
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

	public void sendEmailsContributorsSharedLinks() {
		List<Contributor> contributors = contributorRepository.findContributorFromCommitFilesWithCopiedLines();
		contributors = contributors.stream().filter(c -> !KnowledgeIslandsUtils.checkIfEmailNoreply(c.getEmail())).toList();
		for (Contributor contributor : contributors) {
			String subject = emailService.getSubjectEmailSurveyGenAI(); //emailService.getSubjectEmailSurveyGoogleForm();
			String text = emailService.getTextEmailSurveyGenAIRawText(contributor.getName());
			//			if(emailService.sendEmail(contributor.getEmail(), subject, text)) {
			//				contributor.setEmailSharedLinkSent(true);
			//				contributorRepository.save(contributor);
			//			}
		}
	}
}
