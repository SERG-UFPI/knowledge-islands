package br.com.gitanalyzer.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gitanalyzer.model.entity.FileRepositorySharedLinkCommit;
import br.com.gitanalyzer.repository.FileRepositorySharedLinkCommitRepository;

@Service
public class FileRepositorySharedLinkCommitService {

	@Autowired
	private FileRepositorySharedLinkCommitRepository repository;

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

}
