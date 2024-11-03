package br.com.gitanalyzer.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.ContributorGenAiUse;
import br.com.gitanalyzer.model.entity.SharedLinkCommit;
import br.com.gitanalyzer.repository.ContributorGenAiUseRepository;
import br.com.gitanalyzer.repository.SharedLinkCommitRepository;

@Service
public class ContributorGenAiUseService {

	@Autowired
	private ContributorGenAiUseRepository contributorGenAiUseRepository;
	@Autowired
	private SharedLinkCommitRepository sharedLinkCommitRepository;

	@Transactional
	public void createContributorGenAiUse() {
		List<SharedLinkCommit> sharedLinksCommits = sharedLinkCommitRepository.findByCommitFileAddedLinkIsNotNull();
		Map<Contributor, List<SharedLinkCommit>> map = sharedLinksCommits.stream().collect(Collectors.groupingBy(slc -> slc.getCommitFileAddedLink().getCommit().getAuthor()));
		for (Map.Entry<Contributor, List<SharedLinkCommit>> entry: map.entrySet()) {
			int totalNumCopiedLines = 0;
			for (SharedLinkCommit sharedLinkCommit : entry.getValue()) {
				totalNumCopiedLines = totalNumCopiedLines+sharedLinkCommit.getCopiedLines().size();
			}
			contributorGenAiUseRepository.save(new ContributorGenAiUse(entry.getKey(), totalNumCopiedLines, totalNumCopiedLines/entry.getValue().size()));
		}
	}
}
