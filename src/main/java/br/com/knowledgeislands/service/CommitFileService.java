package br.com.knowledgeislands.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.knowledgeislands.model.entity.Commit;
import br.com.knowledgeislands.model.entity.CommitFile;
import br.com.knowledgeislands.model.entity.Contributor;
import br.com.knowledgeislands.model.entity.File;

@Service
public class CommitFileService {

	@Autowired
	private ContributorService contributorService;

	public List<CommitFile> getAllCommitFilesOfAuthor(File file, Contributor contributor){
		List<CommitFile> commitFilesAuthor = new ArrayList<>();
		for (Commit commit : file.getCommits()) {
			if(commit.getAuthor().getEmail().equals(contributor.getEmail()) 
					|| contributorService.checkAliasContributors(contributor, commit.getAuthor())) {
				commitFilesAuthor.add(commit.getCommitFiles().stream().filter(cf -> cf.getFile().getPath().equals(file.getPath())).findFirst().get());
				continue;
			}
		}
		return commitFilesAuthor;
	}

	public void setCommitFileGenai() {
		
		
	}
}
