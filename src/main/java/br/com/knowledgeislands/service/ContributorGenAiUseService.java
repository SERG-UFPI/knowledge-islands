package br.com.knowledgeislands.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.knowledgeislands.model.entity.Contributor;
import br.com.knowledgeislands.model.entity.ContributorGenAiUse;
import br.com.knowledgeislands.model.entity.GlobalGenAiUse;
import br.com.knowledgeislands.model.entity.SharedLinkCommit;
import br.com.knowledgeislands.repository.ContributorGenAiUseRepository;
import br.com.knowledgeislands.repository.ContributorRepository;
import br.com.knowledgeislands.repository.GlobalGenAiUseRepository;
import br.com.knowledgeislands.repository.SharedLinkCommitRepository;

@Service
public class ContributorGenAiUseService {

	@Autowired
	private ContributorGenAiUseRepository contributorGenAiUseRepository;
	@Autowired
	private SharedLinkCommitRepository sharedLinkCommitRepository;
	@Autowired
	private GlobalGenAiUseRepository globalGenAiUseRepository;
	@Autowired
	private ContributorRepository contributorRepository;

	@Transactional
	public void createContributorGenAiUseSharedLink() {
		List<ContributorGenAiUse> contributosGenAiUses = new ArrayList<>();
		List<SharedLinkCommit> sharedLinksCommits = sharedLinkCommitRepository.findSharedLinkWithCopiedLinesMoreThanOne();
		Map<Contributor, List<SharedLinkCommit>> map = sharedLinksCommits.stream().collect(Collectors.groupingBy(slc -> slc.getCommitFileAddedLink().getCommit().getAuthor()));
		for (Map.Entry<Contributor, List<SharedLinkCommit>> entry: map.entrySet()) {
			int totalNumCopiedLines = 0;
			double sumAvgCopied = 0.0;
			int numCopiedLinks = 0;
			for (SharedLinkCommit sharedLinkCommit : entry.getValue()) {
				if(sharedLinkCommit.getNumberCopiedLines() > 0) {
					totalNumCopiedLines += sharedLinkCommit.getNumberCopiedLines();
					sumAvgCopied += (double)sharedLinkCommit.getNumberCopiedLines()/sharedLinkCommit.getCommitFileAddedLink().getAdditionsCodes();
					numCopiedLinks++;
				}
			}
			if(totalNumCopiedLines > 0) {
				Contributor contributor = entry.getKey();
				ContributorGenAiUse contributorGenAiUse = contributorGenAiUseRepository.save(new ContributorGenAiUse(contributor, totalNumCopiedLines, 
						sumAvgCopied/numCopiedLinks));
				contributor.setContributorGenAiUse(contributorGenAiUse);
				contributorRepository.save(contributor);
				contributosGenAiUses.add(contributorGenAiUse);
			}
		}
		int numCopiedCode = 0;
		int numContributorCopied = 0;
		double avgPctCopiedCode = 0.0;
		numContributorCopied = contributosGenAiUses.size();
		for (ContributorGenAiUse contributorGenAiUse : contributosGenAiUses) {
			numCopiedCode = numCopiedCode+contributorGenAiUse.getTotalNumCopiedLines();
			avgPctCopiedCode = avgPctCopiedCode+contributorGenAiUse.getAvgCopiedLinesCommits();
		}
		avgPctCopiedCode = avgPctCopiedCode/contributosGenAiUses.size();
		globalGenAiUseRepository.save(new GlobalGenAiUse(numCopiedCode, numContributorCopied, avgPctCopiedCode));
	}

	@Transactional
	public void createContributorGenAiUse() {
		GlobalGenAiUse globalGenAiUse = globalGenAiUseRepository.findAll().get(0);
		List<Contributor> contributors = contributorRepository.findContributorNotExistsContributorGenAiUse();
		for (Contributor contributor : contributors) {
			ContributorGenAiUse contributorGenAiUse = new ContributorGenAiUse(contributor, 0, 
					globalGenAiUse.getAvgPctCopiedCode());
			contributorGenAiUseRepository.save(contributorGenAiUse);
			contributor.setContributorGenAiUse(contributorGenAiUse);
			contributorRepository.save(contributor);
		}
	}

	public void createContributorGenAiUseFull() {
		createContributorGenAiUseSharedLink();
		createContributorGenAiUse();
	}

	@Transactional
	public void fixCreateContributorGenAiUse() {
		List<ContributorGenAiUse> contributorGenAiUses = contributorGenAiUseRepository.findAll();
		for (ContributorGenAiUse contributorGenAiUse : contributorGenAiUses) {
			Contributor contributor = contributorGenAiUse.getContributor();
			contributor.setContributorGenAiUse(contributorGenAiUse);
			contributorRepository.save(contributor);
		}
	}

}
