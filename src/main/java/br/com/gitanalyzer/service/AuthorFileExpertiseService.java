package br.com.gitanalyzer.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.model.entity.AuthorFileExpertise;
import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.entity.GitRepositoryVersion;
import br.com.gitanalyzer.model.entity.GitRepositoryVersionKnowledgeModel;
import br.com.gitanalyzer.model.entity.SharedLinkCommit;
import br.com.gitanalyzer.repository.AuthorFileExpertiseRepository;
import br.com.gitanalyzer.repository.SharedLinkCommitRepository;

@Service
public class AuthorFileExpertiseService {

	@Autowired
	private SharedLinkCommitRepository sharedLinkCommitRepository;
	@Autowired
	private AuthorFileExpertiseRepository repository;

	public void exportAuthorFileExpertiseSharedLink() {
		List<SharedLinkCommit> sharedLinkCommits = new ArrayList<>();//sharedLinkCommitRepository.findByCommitFileAddedLinkWithRemovedLines();
		Map<GitRepository, List<SharedLinkCommit>> sharedLinksByRepo = new HashMap<>();
		for (SharedLinkCommit sharedLinkCommit : sharedLinkCommits) {
			GitRepository gitRepository = sharedLinkCommit.getFileRepositorySharedLinkCommit().getGitRepository();
			sharedLinksByRepo.computeIfAbsent(gitRepository, k -> new ArrayList<>()).add(sharedLinkCommit);
		}
		for(Map.Entry<GitRepository, List<SharedLinkCommit>> entry: sharedLinksByRepo.entrySet()) {
			List<SharedLinkCommit> sharedLinks = entry.getValue();
			Map<ContributorFile, List<SharedLinkCommit>> contributorFileLinks = new HashMap<>();
			for (SharedLinkCommit sharedLinkCommit : sharedLinks) {
				ContributorFile contributorFile = 
						new ContributorFile(sharedLinkCommit.getCommitFileAddedLink().getCommit().getAuthor(), 
								sharedLinkCommit.getFileRepositorySharedLinkCommit().getFile());
				contributorFileLinks.computeIfAbsent(contributorFile, k -> new ArrayList<>()).add(sharedLinkCommit);
			}
			for(Map.Entry<ContributorFile, List<SharedLinkCommit>> entry2: contributorFileLinks.entrySet()) {
				ContributorFile contributorFile = entry2.getKey();
				List<AuthorFileExpertise> authorFilesExpertises = new ArrayList<>();
				for(GitRepositoryVersion version: entry.getKey().getGitRepositoryVersion()) {
					GitRepositoryVersionKnowledgeModel model = version.getGitRepositoryVersionKnowledgeModel().get(0);
					for (AuthorFileExpertise authorFileExpertise : model.getAuthorsFiles()) {
						if(authorFileExpertise.getContributorVersion().getContributor().getId().equals(contributorFile.contributor.getId()) && 
								authorFileExpertise.getFileVersion().getId().equals(contributorFile.file.getId())) {
							authorFilesExpertises.add(authorFileExpertise);
						}
					}
				}
			}
		}
		//TODO CREATE EXPORT AUTHOR FILE EXPERTISE
	}

	private class ContributorFile{
		Contributor contributor;
		File file;
		public ContributorFile(Contributor contributor, File file) {
			super();
			this.contributor = contributor;
			this.file = file;
		}
	}

}
