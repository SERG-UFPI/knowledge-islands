package br.com.gitanalyzer.service;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gitanalyzer.model.entity.CodeLine;
import br.com.gitanalyzer.model.entity.Commit;
import br.com.gitanalyzer.model.entity.CommitFile;
import br.com.gitanalyzer.model.entity.FileRepositorySharedLinkCommit;
import br.com.gitanalyzer.model.entity.GitRepositoryVersion;
import br.com.gitanalyzer.model.entity.SharedLinkCommit;
import br.com.gitanalyzer.repository.CommitFileRepository;
import br.com.gitanalyzer.repository.FileRepositorySharedLinkCommitRepository;
import br.com.gitanalyzer.repository.SharedLinkCommitRepository;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class SharedLinkCommitService {

	@Autowired
	private FileRepositorySharedLinkCommitRepository fileGitRepositorySharedLinkCommitRepository;
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
		List<FileRepositorySharedLinkCommit> filesSharedLinksCommits = fileGitRepositorySharedLinkCommitRepository
				.findByGitRepositoryId(gitRepositoryVersion.getGitRepository().getId());
		List<Commit> commits = gitRepositoryVersion.getCommits();
		Collections.sort(commits);
		try(Git git = Git.open(new File(gitRepositoryVersion.getGitRepository().getCurrentFolderPath()));) {
			Repository repository = git.getRepository();
			for (FileRepositorySharedLinkCommit fileGitRepositorySharedLinkCommit : filesSharedLinksCommits) {
				//				if(fileGitRepositorySharedLinkCommit.getFile().getPath().equals("Programers/Day5/42626_\\354\\212\\244\\354\\275\\224\\353\\271\\214.java")) {
				//					System.out.println();
				//				}
				for (Commit commit : commits) {
					//					if(commit.getSha().equals("86119b6991e4a5789002fbde85109be279aa9395")) {
					//						System.out.println();
					//					}
					for (CommitFile commitFile : commit.getCommitFiles()) {
						if(commitFile.getFile().isFile(fileGitRepositorySharedLinkCommit.getFile().getPath())) {
							List<String> addedCodeLines = commitService.getCodeLinesAddedCommitFile(repository, commit, fileGitRepositorySharedLinkCommit.getFile());
							commitFile.setAdditionsCodes(addedCodeLines.size());
							if(addedCodeLines != null && !addedCodeLines.isEmpty()) {
								addedCodeLines.forEach(c -> commitFile.getAddedCodeLines().add(new CodeLine(c)));
								List<SharedLinkCommit> sharedLinksCommits = fileGitRepositorySharedLinkCommit.getSharedLinksCommits().stream().filter(slc -> slc.getSharedLink().getConversation() != null).toList();
								for(SharedLinkCommit sharedLinkCommit: sharedLinksCommits) {
									if(addedCodeLines.stream().anyMatch(l -> l.contains(sharedLinkCommit.getSharedLink().getLink())) && 
											sharedLinkCommit.getCommitFileAddedLink() == null) {
										List<String> chatGPTCodeLines = chatGPTConversationService.getCodesFromConversation(sharedLinkCommit.getSharedLink().getConversation().getConversationTurns());
										List<String> codeLinesCopied = chatGPTConversationService.getLinesCopied(chatGPTCodeLines, addedCodeLines);
										if(codeLinesCopied != null && !codeLinesCopied.isEmpty()) {
											codeLinesCopied.forEach(c -> sharedLinkCommit.getCopiedLines().add(new CodeLine(c)));
											commitFile.setRemovingsCodes(codeLinesCopied.size());
											commitFile.setAdditions(commitFile.getAdditions()-codeLinesCopied.size());
											commitFile.setAdditionsCodes(commitFile.getAdditionsCodes()-codeLinesCopied.size());
										}
										sharedLinkCommit.setCommitFileAddedLink(commitFile);
										sharedLinkCommitRepository.save(sharedLinkCommit);
									}
								}
								if(commitFile.getAdditions() < 0) {
									System.out.println();
								}
								commitFileRepository.save(commitFile);
							}
						}
					}
				}
				for(SharedLinkCommit sharedLinkCommit: fileGitRepositorySharedLinkCommit.getSharedLinksCommits()) {
					if(sharedLinkCommit.getSharedLink().getConversation() != null && 
							sharedLinkCommit.getCommitFileAddedLink() == null) {
						System.out.println();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}

}
