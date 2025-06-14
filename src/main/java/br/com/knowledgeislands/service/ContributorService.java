package br.com.knowledgeislands.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.knowledgeislands.model.entity.Commit;
import br.com.knowledgeislands.model.entity.Contributor;
import br.com.knowledgeislands.model.entity.GitRepository;
import br.com.knowledgeislands.model.entity.GitRepositoryVersion;
import br.com.knowledgeislands.model.entity.SharedLinkCommit;
import br.com.knowledgeislands.repository.AttemptSendEmailRepository;
import br.com.knowledgeislands.repository.ContributorRepository;
import br.com.knowledgeislands.repository.GitRepositoryRepository;
import br.com.knowledgeislands.repository.GitRepositoryVersionRepository;
import br.com.knowledgeislands.repository.SharedLinkCommitRepository;
import br.com.knowledgeislands.utils.KnowledgeIslandsUtils;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ContributorService {

	@Autowired
	private EmailService emailService;
	@Autowired
	private ContributorRepository contributorRepository;
	@Autowired
	private GitRepositoryRepository gitRepositoryRepository;
	@Autowired
	private GitRepositoryVersionRepository gitRepositoryVersionRepository;
	@Autowired
	private SharedLinkCommitRepository sharedLinkCommitRepository; 
	@Autowired
	private AttemptSendEmailRepository attemptSendEmailRepository;
	@Value("${spring.mail.username}")
	private String email;

	public List<Contributor> setActiveContributors(List<Contributor> contributors, List<Commit> commits, Date dateVersion){
		if(contributors != null && !contributors.isEmpty()) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dateVersion);
			calendar.add(Calendar.YEAR, -1);
			Date date = calendar.getTime();
			commits = commits.stream().filter(c -> c.getAuthorDate().after(date)).toList();
			for (Contributor contributor : contributors) {
				for (Commit commit : commits) {
					if (commit.getAuthor().equals(contributor)) {
						contributor.setActive(true);
						contributorRepository.save(contributor);
						break;
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

	//@Scheduled(fixedRate = 90000000)
	@Transactional
	public void sendEmailsContributorsSharedLinks() {
		List<SharedLinkCommit> sharedLinksCommits = sharedLinkCommitRepository.findSharedLinkWithCopiedLinesMoreThanOne();
		Map<Contributor, List<SharedLinkCommit>> map = sharedLinksCommits.stream().collect(Collectors.groupingBy(slc -> slc.getAuthor()));
		List<SharedLinkCommit> toSend = new ArrayList<>();
		for (Map.Entry<Contributor, List<SharedLinkCommit>> entry : map.entrySet()) {
			Contributor contributor = entry.getKey();
			if(KnowledgeIslandsUtils.checkIfEmailNoreply(contributor.getEmail())) continue;
			SharedLinkCommit slcToSend = entry.getValue().get(0);
			for (SharedLinkCommit slc : entry.getValue()) {
				if(slc.getNumberCopiedLines() > slcToSend.getNumberCopiedLines()) {
					slcToSend = slc;
				}
			}
			toSend.add(slcToSend);
		}
		log.info("");
//		int max = 200;
//		int i = 0;
//		for (SharedLinkCommit sharedLinkCommit : toSend) {
//			if(i >= max) break;
//			Contributor contributor = sharedLinkCommit.getAuthor();
//			if(!attemptSendEmailRepository.existsByContributorId(contributor.getId())) {
//				emailService.sendSingleEmail(sharedLinkCommit);
//				i++;
//				try {
//					Thread.sleep(2000);
//					if(i % 10 == 0) {
//						log.info("Waiting 1 minute after sending " + i + " e-mails...");
//		                Thread.sleep(60_000);
//					}
//				} catch (InterruptedException e) {
//					Thread.currentThread().interrupt();
//		            log.error("Thread interrompida durante sleep", e);
//				}
//			}
//		}
	}

	@Transactional
	public void setContributorActiveGitRepositoryVersionNotFiltered() {
		List<GitRepository> repositories = gitRepositoryRepository.findByFilteredFalse();
		for (GitRepository gitRepository : repositories) {
			List<GitRepositoryVersion> versions = gitRepositoryVersionRepository.findByGitRepositoryId(gitRepository.getId());
			GitRepositoryVersion version = versions.get(0);
			setActiveContributors(version.getContributors(), version.getCommits(), version.getDateVersion());
		}
	}
}
