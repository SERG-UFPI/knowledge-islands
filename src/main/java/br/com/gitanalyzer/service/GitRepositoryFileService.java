package br.com.gitanalyzer.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gitanalyzer.model.entity.ConversationTurn;
import br.com.gitanalyzer.model.entity.PromptCode;
import br.com.gitanalyzer.model.entity.SharedLink;
import br.com.gitanalyzer.model.enums.ChatgptUserAgent;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class GitRepositoryFileService {

	@Transactional(readOnly = true)
	public String setCommitsOfFilesSharedLinks() {
		//		List<Long> ids = truckFactorRepository.findRepositoriesIds();
		//		List<GitRepository> repositories = gitRepositoryRepository.findAllById(ids);
		//		for (GitRepository gitRepository : repositories) {
		//			Git git = null;
		//			try {
		//				git = Git.open(new java.io.File(gitRepository.getCurrentFolderPath()));
		//			} catch (IOException e) {
		//				e.printStackTrace();
		//			}
		//			Repository repository = git.getRepository();
		//			List<AuthorFile> authorsFilesRepository = new ArrayList<>();
		//			List<Commit> commits = new ArrayList<>();
		//			for(GitRepositoryVersion version: gitRepository.getGitRepositoryVersion()) {
		//				commits.addAll(version.getCommits());
		//				for(GitRepositoryVersionKnowledgeModel model: version.getGitRepositoryVersionKnowledgeModel()) {
		//					model.getAuthorsFiles().forEach(af -> af.setGitRepositoryVersionKnowledgeModel(model));
		//					authorsFilesRepository.addAll(model.getAuthorsFiles());
		//				}
		//			}
		//			for (SharedLink sharedLink : gitRepository.getSharedLinks()) {
		//				try {
		//					List<String> codes = getCodesFromConversation(sharedLink.getConversation().getConversationTurns());
		//					forFileLinkAuthor: for (FileLinkAuthor fileLinkAuthor : sharedLink.getFilesLinkAuthor()) {
		//						LogCommand logCommand = git.log().setRevFilter(RevFilter.NO_MERGES);
		//						logCommand.addPath(fileLinkAuthor.getFile().getPath());
		//						Iterable<RevCommit> commitsIterable = logCommand.call();
		//						if(commitsIterable != null) {
		//							for (RevCommit revCommit : commitsIterable) {
		//								List<DiffEntry> diffsForTheCommit = jgitUtils.diffsForTheCommit(repository, revCommit);
		//								for (DiffEntry diff : diffsForTheCommit) {
		//									if(fileLinkAuthor.getFile().isFile(diff.getNewPath())) {
		//										ByteArrayOutputStream stream = new ByteArrayOutputStream();
		//										DiffFormatter diffFormatter = new DiffFormatter( stream );
		//										diffFormatter.setRepository(repository);
		//										diffFormatter.format(diff);
		//										String in = stream.toString();
		//										List<String> linesAdded = jgitUtils.analyze(in);
		//										if(linesAdded != null && !linesAdded.isEmpty() && linesAdded.stream().anyMatch(l -> l.contains(sharedLink.getLink()))) {
		//											linesAdded.remove(0);
		//											linesAdded = removePlusSign(linesAdded);
		//											fileLinkAuthor.setLinesCopied(getLinesCopied(codes, linesAdded));
		//											try {
		//												Commit commit = commits.stream().filter(c -> c.getSha().equals(revCommit.getName())).findFirst().get();
		//												fileLinkAuthor.setCommitThatAddedTheLink(commit);
		//												//												Contributor author = commit.getAuthor();
		//												//												for(GitRepositoryVersion version: gitRepository.getGitRepositoryVersion()) {
		//												//													for(GitRepositoryVersionKnowledgeModel model: version.getGitRepositoryVersionKnowledgeModel()) {
		//												//														for(AuthorFile authorFile: model.getAuthorsFiles()) {
		//												//															if(authorFile.getFileVersion().getFile().isFile(fileLinkAuthor.getFile().getPath()) &&
		//												//																	authorFile.getAuthorVersion().getContributor().getId().equals(author.getId())) {
		//												//																int newAdds = authorFile.getDoe().getAdds()-fileLinkAuthor.getLinesCopied().size();
		//												//																double newDoeValue = new DoeUtils().getDOE(newAdds, 
		//												//																		authorFile.getDoe().getFa(), authorFile.getDoe().getNumDays(), authorFile.getDoe().getSize());
		//												//																DOE newDoe = new DOE(newAdds, authorFile.getDoe().getFa(), 
		//												//																		authorFile.getDoe().getNumDays(), authorFile.getDoe().getSize(), newDoeValue);
		//												//																AuthorFile newAuthorFile = new AuthorFile(authorFile.getAuthorVersion(), authorFile.getFileVersion(), newDoe);
		//												//																newAuthorFile.setWithGenAi(true);
		//												//																model.getAuthorsFiles().add(newAuthorFile);
		//												//																diffFormatter.flush();
		//												//																diffFormatter.close();
		//												//																continue forFileLinkAuthor;
		//												//															}
		//												//														}
		//												//													}
		//												//												}
		//											}catch(NoSuchElementException e) {
		//												throw new CommitNotFoundBySha(revCommit.getName());
		//											}
		//										}
		//										diffFormatter.flush();
		//										diffFormatter.close();
		//									}
		//								}
		//							}
		//						}else {
		//							throw new NoCommitForFileException(fileLinkAuthor.getFile().getPath());
		//						}
		//						System.out.println("Shared link data not set: "+fileLinkAuthor.getFile().getPath()+ " "+sharedLink.getLink()+" "+gitRepository.getCloneUrl());
		//					}
		//				}catch(IOException | GitAPIException | NoCommitForFileException | CommitNotFoundBySha e) {
		//					e.printStackTrace();
		//				}
		//			}
		//			git.close();
		//		}
		return null;
	}

	@Transactional
	public List<SharedLink> setSharedLinksData(Long gitRepositoryId) {
		//		try {
		//			GitRepository gitRepository = gitRepositoryRepository.findById(gitRepositoryId).get();
		//			Git git = Git.open(new java.io.File(gitRepository.getCurrentFolderPath()));
		//			Repository repository = git.getRepository();
		//			for (GitRepositoryVersion version : gitRepository.getGitRepositoryVersion()) {
		//				for (SharedLink sharedLink : gitRepository.getSharedLinks()) {
		//					try {
		//						List<String> codes = getCodesFromConversation(sharedLink.getConversation().getConversationTurns());
		//						for (FileLinkAuthor fileLinkAuthor : sharedLink.getFilesLinkAuthor()) {
		//							LogCommand logCommand = git.log().setRevFilter(RevFilter.NO_MERGES);
		//							logCommand.addPath(fileLinkAuthor.getFile().getPath());
		//							Iterable<RevCommit> commitsIterable = logCommand.call();
		//							if(commitsIterable != null) {
		//								for (RevCommit revCommit : commitsIterable) {
		//									List<DiffEntry> diffsForTheCommit = jgitUtils.diffsForTheCommit(repository, revCommit);
		//									for (DiffEntry diff : diffsForTheCommit) {
		//										if(fileLinkAuthor.getFile().isFile(diff.getNewPath())) {
		//											ByteArrayOutputStream stream = new ByteArrayOutputStream();
		//											DiffFormatter diffFormatter = new DiffFormatter( stream );
		//											diffFormatter.setRepository(repository);
		//											diffFormatter.format(diff);
		//											String in = stream.toString();
		//											List<String> linesAdded = jgitUtils.analyze(in);
		//											if(linesAdded != null && !linesAdded.isEmpty() && 
		//													linesAdded.stream().anyMatch(l -> l.contains(sharedLink.getLink()))) {
		//												linesAdded.remove(0);
		//												linesAdded = removePlusSign(linesAdded);
		//												fileLinkAuthor.setLinesCopied(getLinesCopied(codes, linesAdded));
		//												if(fileLinkAuthor.getId() == null) {
		//													fileLinkAuthorRepository.save(fileLinkAuthor);
		//												}
		//												Commit commit = version.getCommits().stream().filter(c -> c.getSha().equals(revCommit.getName())).findFirst().get();
		//												if(fileLinkAuthor.getCommitThatAddedTheLink() == null) {
		//													fileLinkAuthor.setCommitThatAddedTheLink(commit);
		//													sharedLinkRepository.save(sharedLink);
		//												}
		//											}
		//											diffFormatter.flush();
		//											diffFormatter.close();
		//										}
		//									}
		//								}
		//							}else {
		//								throw new NoCommitForFileException(fileLinkAuthor.getFile().getPath());
		//							}
		//							System.out.println("Shared link data not set: "+fileLinkAuthor.getFile().getPath()+ " "+sharedLink.getLink()+" "+gitRepository.getCloneUrl());
		//						}
		//					}catch(IOException | GitAPIException | NoCommitForFileException e) {
		//						e.printStackTrace();
		//					}
		//				}
		//			}
		//			git.close();
		//			return gitRepository.getSharedLinks();
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
		return null;
	}

	private List<String> getLinesCopied(List<String> code, List<String> addedLines) {
		List<String> linesCopied = new ArrayList<>();
		addedLineFor: for (String addedLine: addedLines) {
			if(!addedLine.isBlank()) {
				for(String lineCode : code) {
					if(!lineCode.isBlank() && lineCode.trim().equals(addedLine.trim())) {
						linesCopied.add(addedLine);
						continue addedLineFor;
					}
				}
			}
		}
		return linesCopied;
	}

	private List<String> removePlusSign(List<String> lines) {
		List<String> modifiedLines = new ArrayList<>();
		for (String line : lines) {
			if (line.startsWith("+")) {
				modifiedLines.add(line.substring(1).trim());
			} else {
				modifiedLines.add(line);
			}
		}
		return modifiedLines;
	}

	private List<String> getCodesFromConversation(List<ConversationTurn> conversation){
		List<String> codes = new ArrayList<>();
		for (ConversationTurn turn : conversation) {
			if(turn.getUserAgent().equals(ChatgptUserAgent.ASSISTANT) && turn.getCodes() != null
					&& turn.getCodes().size() > 0) {
				for(PromptCode code: turn.getCodes()) {
					codes.addAll(Arrays.asList(code.getCode().split("\n")));
				}
			}
		}
		return codes;
	}
}
