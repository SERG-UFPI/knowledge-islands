package br.com.gitanalyzer.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gitanalyzer.model.entity.CodeLine;
import br.com.gitanalyzer.model.entity.Commit;
import br.com.gitanalyzer.model.entity.CommitFile;
import br.com.gitanalyzer.model.entity.FileGitRepositorySharedLinkCommit;
import br.com.gitanalyzer.model.entity.GitRepositoryVersion;
import br.com.gitanalyzer.model.entity.SharedLinkCommit;
import br.com.gitanalyzer.repository.CommitFileRepository;
import br.com.gitanalyzer.repository.FileGitRepositorySharedLinkCommitRepository;
import br.com.gitanalyzer.repository.SharedLinkCommitRepository;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class SharedLinkCommitService {

	@Autowired
	private FileGitRepositorySharedLinkCommitRepository fileGitRepositorySharedLinkCommitRepository;
	@Autowired
	private CommitService commitService;
	@Autowired
	private ChatGPTConversationService chatGPTConversationService;
	@Autowired
	private CommitFileRepository commitFileRepository;
	@Autowired
	private SharedLinkCommitRepository sharedLinkCommitRepository;

	@Transactional
	public void setCommitCopiedLineOfRepository(GitRepositoryVersion gitRepositoryVersion) {
		List<FileGitRepositorySharedLinkCommit> filesSharedLinksCommits = fileGitRepositorySharedLinkCommitRepository
				.findByGitRepositoryId(gitRepositoryVersion.getGitRepository().getId());
		for (FileGitRepositorySharedLinkCommit fileGitRepositorySharedLinkCommit : filesSharedLinksCommits) {
			try(Git git = Git.open(new File(fileGitRepositorySharedLinkCommit.getGitRepository().getCurrentFolderPath()));) {
				Repository repository = git.getRepository();
				for (Commit commit : gitRepositoryVersion.getCommits()) {
					for (CommitFile commitFile : commit.getCommitFiles()) {
						if(commitFile.getFile().isFile(fileGitRepositorySharedLinkCommit.getFile().getPath())) {
							List<String> addedLines = commitService.getLinesAddedCommitFile(repository, commit, fileGitRepositorySharedLinkCommit.getFile());
							commitFile.setAdditionsCodes(addedLines.size());
							if(addedLines != null && !addedLines.isEmpty()) {
								addedLines.forEach(c -> commitFile.getAddedCodeLines().add(new CodeLine(c)));
								for(SharedLinkCommit sharedLinkCommit: fileGitRepositorySharedLinkCommit.getSharedLinksCommits()) {
									if(addedLines.stream().anyMatch(l -> l.contains(sharedLinkCommit.getSharedLink().getLink())) && 
											sharedLinkCommit.getCommitFileThatAddedTheLink() == null) {
										sharedLinkCommit.setCommitFileThatAddedTheLink(commitFile);
										List<String> chatGPTCodeLines = chatGPTConversationService.getCodesFromConversation(sharedLinkCommit.getSharedLink().getConversation().getConversationTurns());
										List<String> codeLinesCopied = chatGPTConversationService.getLinesCopied(chatGPTCodeLines, addedLines);
										if(codeLinesCopied != null && !codeLinesCopied.isEmpty()) {
											codeLinesCopied.forEach(c -> sharedLinkCommit.getCopiedLines().add(new CodeLine(c)));
											commitFile.setAdditions(commitFile.getAdditions()-codeLinesCopied.size());
											commitFile.setAdditionsCodes(commitFile.getAdditionsCodes()-codeLinesCopied.size());
										}
										sharedLinkCommitRepository.save(sharedLinkCommit);
									}
								}
								commitFileRepository.save(commitFile);
							}
						}
					}
				}
				for(SharedLinkCommit sharedLinkCommit: fileGitRepositorySharedLinkCommit.getSharedLinksCommits()) {
					if(sharedLinkCommit.getCommitFileThatAddedTheLink() == null) {
						System.out.println();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				log.error(e.getMessage());
			}
		}
	}

}
