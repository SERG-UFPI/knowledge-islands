package br.com.gitanalyzer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.ContributorGenAiUse;
import br.com.gitanalyzer.model.entity.GlobalGenAiUse;
import br.com.gitanalyzer.model.entity.SharedLinkCommit;
import br.com.gitanalyzer.repository.ContributorGenAiUseRepository;
import br.com.gitanalyzer.repository.ContributorRepository;
import br.com.gitanalyzer.repository.GlobalGenAiUseRepository;
import br.com.gitanalyzer.repository.SharedLinkCommitRepository;

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
		List<SharedLinkCommit> sharedLinksCommits = sharedLinkCommitRepository.findByCommitFileAddedLinkIsNotNull();
		Map<Contributor, List<SharedLinkCommit>> map = sharedLinksCommits.stream().collect(Collectors.groupingBy(slc -> slc.getCommitFileAddedLink().getCommit().getAuthor()));
		for (Map.Entry<Contributor, List<SharedLinkCommit>> entry: map.entrySet()) {
			int totalNumCopiedLines = 0;
			double sumAvgCopied = 0.0;
			int numCopiedLinks = 0;
			for (SharedLinkCommit sharedLinkCommit : entry.getValue()) {
				if(sharedLinkCommit.getNumberCopiedLines() > 0) {
					totalNumCopiedLines = totalNumCopiedLines+sharedLinkCommit.getNumberCopiedLines();
					sumAvgCopied = sumAvgCopied+(double)sharedLinkCommit.getNumberCopiedLines()/sharedLinkCommit.getCommitFileAddedLink().getAdditionsCodes();
					numCopiedLinks++;
				}
			}
			if(totalNumCopiedLines > 0) {
				contributosGenAiUses.add(contributorGenAiUseRepository.save(new ContributorGenAiUse(entry.getKey(), totalNumCopiedLines, 
						sumAvgCopied/numCopiedLinks)));
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
			contributorGenAiUseRepository.save(new ContributorGenAiUse(contributor, 0, 
					globalGenAiUse.getAvgPctCopiedCode()));
		}
	}

	public void createContributorGenAiUseFull() {
		createContributorGenAiUseSharedLink();
		createContributorGenAiUse();
	}

}
