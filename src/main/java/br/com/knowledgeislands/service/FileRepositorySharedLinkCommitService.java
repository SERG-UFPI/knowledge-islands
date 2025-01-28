package br.com.knowledgeislands.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.knowledgeislands.model.entity.FileRepositorySharedLinkCommit;
import br.com.knowledgeislands.model.entity.SharedLinkCommit;
import br.com.knowledgeislands.repository.FileRepositorySharedLinkCommitRepository;
import br.com.knowledgeislands.repository.SharedLinkCommitRepository;

@Service
public class FileRepositorySharedLinkCommitService {

	@Autowired
	private FileRepositorySharedLinkCommitRepository repository;
	@Autowired
	private SharedLinkCommitRepository sharedLinkCommitRepository;

	@Transactional
	public void exportFileRepositorySharedLinkCommit() throws IOException {
		File jsonFile = new File("/home/otavio/fileSharedLinks.json");
		ObjectMapper mapper = new ObjectMapper();
		List<FileRepositorySharedLinkCommit> filesLinks = repository.findWithNonNullConversation();
		for (FileRepositorySharedLinkCommit fileRepositorySharedLinkCommit : filesLinks) {
			fileRepositorySharedLinkCommit.setSharedLinksCommits(
					fileRepositorySharedLinkCommit.getSharedLinksCommits().stream().filter(slc -> slc.getSharedLink().getConversation() != null).toList());
		}
		mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, filesLinks);
	}

	@Transactional
	public void fixFileRepositorySharedLinkCommit() {
		List<FileRepositorySharedLinkCommit> toBeRemovedAux = new ArrayList<>();
		List<FileRepositorySharedLinkCommit> filesRepositories = repository.findEntitiesWithDuplicateFiles();
		for (int i = 0; i < filesRepositories.size(); i++) {
			for (int j = i+1; j < filesRepositories.size(); j++) {
				if(filesRepositories.get(i).getFile().getId().equals(filesRepositories.get(j).getFile().getId()) && 
						filesRepositories.get(i).getGitRepository().getId().equals(filesRepositories.get(j).getGitRepository().getId())) {
					toBeRemovedAux.add(filesRepositories.get(j));
				}
			}
		}
		filesRepositories.removeAll(toBeRemovedAux);
		List<FileRepositorySharedLinkCommit> toBeRemoved = new ArrayList<>();
		for (FileRepositorySharedLinkCommit fileRepositorySharedLinkCommit : filesRepositories) {
			List<FileRepositorySharedLinkCommit> filesRepositoriesAux = repository.findByFileAndGitRepositoryExcludingId(fileRepositorySharedLinkCommit.getFile().getId(),
					fileRepositorySharedLinkCommit.getGitRepository().getId(), fileRepositorySharedLinkCommit.getId());
			if(filesRepositoriesAux != null && !filesRepositoriesAux.isEmpty()) {
				for (FileRepositorySharedLinkCommit fileRepositorySharedLinkCommit2 : filesRepositoriesAux) {
					for (SharedLinkCommit slc : fileRepositorySharedLinkCommit2.getSharedLinksCommits()) {
						slc.setFileRepositorySharedLinkCommit(fileRepositorySharedLinkCommit);
						sharedLinkCommitRepository.save(slc);
					}
					fileRepositorySharedLinkCommit2.getSharedLinksCommits().clear();
					repository.save(fileRepositorySharedLinkCommit2);
					toBeRemoved.add(fileRepositorySharedLinkCommit2);
				}
			}
		}
		repository.deleteAll(toBeRemoved);
	}

}
