package br.com.gitanalyzer.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gitanalyzer.analysis.dev_gpt.DevGptSearches;
import br.com.gitanalyzer.analysis.dev_gpt.GitHubCall;
import br.com.gitanalyzer.exceptions.FetchPageException;
import br.com.gitanalyzer.exceptions.SharedLinkNoCode;
import br.com.gitanalyzer.exceptions.SharedLinkNoConversation;
import br.com.gitanalyzer.exceptions.SharedLinkNotFoundException;
import br.com.gitanalyzer.model.entity.ChatgptConversation;
import br.com.gitanalyzer.model.entity.ConversationTurn;
import br.com.gitanalyzer.model.entity.ErrorLog;
import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.entity.GitRepositoryFile;
import br.com.gitanalyzer.model.entity.PromptCode;
import br.com.gitanalyzer.model.entity.SharedLink;
import br.com.gitanalyzer.model.entity.SharedLinkCommit;
import br.com.gitanalyzer.model.entity.SharedLinkErrorLog;
import br.com.gitanalyzer.model.entity.SharedLinkSearch;
import br.com.gitanalyzer.model.enums.ChatgptUserAgent;
import br.com.gitanalyzer.model.enums.SharedLinkFetchError;
import br.com.gitanalyzer.model.enums.SharedLinkSourceType;
import br.com.gitanalyzer.repository.FileRepository;
import br.com.gitanalyzer.repository.GitRepositoryFileRepository;
import br.com.gitanalyzer.repository.GitRepositoryRepository;
import br.com.gitanalyzer.repository.SharedLinkRepository;
import br.com.gitanalyzer.repository.SharedLinkSearchRepository;
import br.com.gitanalyzer.repository.TruckFactorRepository;
import br.com.gitanalyzer.utils.AsyncUtils;
import br.com.gitanalyzer.utils.Constants;
import br.com.gitanalyzer.utils.FileUtils;

@Service
public class SharedLinkService {

	@Autowired
	private SharedLinkRepository repository;
	@Autowired
	private GitRepositoryFileRepository gitRepositoryFileRepository;
	@Autowired
	private GitRepositoryRepository gitRepositoryRepository;
	@Autowired
	private FileRepository fileRepository;
	@Autowired
	private TruckFactorRepository truckFactorRepository;
	@Autowired
	private SharedLinkRepository sharedLinkRepository;
	@Autowired
	private SharedLinkSearchRepository sharedLinkSearchRepository;
	@Value("${configuration.github.token}")
	private String token;

	public void saveFileSharedLinkFull() throws Exception{
		List<GitRepository> gitRepositories = new ArrayList<>(); 
		List<SharedLink> sharedLinks = saveFileSharedLinks();
		//		for (SharedLink sharedLink : sharedLinks) {
		//			if(!gitRepositories.stream().anyMatch(g -> g.getFullName().equals(sharedLink.getRepository().getFullName()))) {
		//				gitRepositories.add(sharedLink.getRepository());
		//			}
		//		}
		saveGitRepositoriesFromApi(gitRepositories);
	}

	public void saveReposFileSharedLinks() throws InterruptedException, IOException {
		List<GitRepository> gitRepositories = new ArrayList<>(); 
		List<SharedLink> sharedLinks = repository.findAll();
		//		for (SharedLink sharedLink : sharedLinks) {
		//			if(!gitRepositories.stream().anyMatch(g -> g.getFullName().equals(sharedLink.getRepository().getFullName()))) {
		//				gitRepositories.add(sharedLink.getRepository());
		//			}
		//		}
		saveGitRepositoriesFromApi(gitRepositories);
		validateAndRemoveRepositories();
	}

	@Transactional
	private void validateAndRemoveRepositories() {
		List<Long> ids = new ArrayList<>();
		List<GitRepository> repositories = gitRepositoryRepository.findAll();
		for (GitRepository gitRepository : repositories) {
			if(gitRepository.getCloneUrl() == null || gitRepository.getLanguage() == null) {
				ids.add(gitRepository.getId());
			}
		}
		//		if(!ids.isEmpty()) {
		//			for (Long id : ids) {
		//				List<SharedLink> links = repository.findByRepositoryId(id);
		//				repository.deleteAll(links);
		//			}
		//			gitRepositoryRepository.deleteAllById(ids);
		//		}
	}

	@Transactional
	public List<GitRepository> saveGitRepositoriesFromApi(List<GitRepository> repositories) throws InterruptedException, IOException{
		ObjectMapper objectMapper = new ObjectMapper();
		ExecutorService executorService = AsyncUtils.getExecutorServiceMax();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (GitRepository gitRepository : repositories) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
				try {
					if(gitRepository.getCloneUrl() == null) {
						String[] command = {"curl", "-H", "Authorization: Bearer "+token, 
								Constants.githubApiBaseUrl+"/repos/"+gitRepository.getFullName()};
						String content = GitHubCall.generalCall(command);
						JsonNode rootNode = objectMapper.readTree(content);
						gitRepository.setPrivateRepository(rootNode.get("private").asBoolean());
						gitRepository.setCloneUrl(rootNode.get("clone_url").asText());
						gitRepository.setLanguage(rootNode.get("language").asText());
						gitRepository.setDefaultBranch(rootNode.get("default_branch").asText());
						gitRepository.setSize(rootNode.get("size").asInt());
						gitRepositoryRepository.save(gitRepository);
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}, executorService);
			futures.add(future);
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		executorService.shutdown();
		return repositories;
	}

	@Transactional
	public void setConversationSharedLinks() {
		List<SharedLink> sharedLinks = sharedLinkRepository.findByConversationIsNullAndErrorIsNull();
		int i = 0;
		for (int j = 0; j < sharedLinks.size(); j++) {
			SharedLink sharedLink = sharedLinks.get(j);
			try {
				String json = DevGptSearches.getOpenAiJson(sharedLink.getLink());
				ChatgptConversation conversation = DevGptSearches.getConversationOfOpenAiJson(json);
				if (conversation.getConversationTurns().size() == 0) {
					throw new SharedLinkNoConversation(sharedLink.getLink());
				}
				for(ConversationTurn conversationTurn: conversation.getConversationTurns()) {
					if(conversationTurn.getCodes() != null && conversationTurn.getCodes().size() > 0) {
						conversationTurn.setCodes(conversationTurn.getCodes().stream().filter(c -> c.getLanguage()!=null).toList());
					}
				}
				if(conversation.getConversationTurns().stream().anyMatch(c -> c.getCodes() != null && c.getCodes().size() > 0) == false) {
					throw new SharedLinkNoCode(sharedLink.getLink());
				}
				sharedLink.setConversation(conversation);
			}catch (SharedLinkNotFoundException e) {
				e.printStackTrace();
				SharedLinkErrorLog errorLog = new SharedLinkErrorLog(SharedLinkFetchError.NOT_FOUND, new ErrorLog(e.getMessage(), new Date()));
				sharedLink.setError(errorLog);
			} catch (FetchPageException e) {
				e.printStackTrace();
				SharedLinkErrorLog errorLog = new SharedLinkErrorLog(SharedLinkFetchError.FETCH_ERROR, new ErrorLog(e.getMessage(), new Date()));
				sharedLink.setError(errorLog);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				SharedLinkErrorLog errorLog = new SharedLinkErrorLog(SharedLinkFetchError.JSON_ERROR, new ErrorLog(e.getMessage(), new Date()));
				sharedLink.setError(errorLog);
			} catch (SharedLinkNoConversation e) {
				e.printStackTrace();
				SharedLinkErrorLog errorLog = new SharedLinkErrorLog(SharedLinkFetchError.LINK_NO_CONVERSATION, new ErrorLog(e.getMessage(), new Date()));
				sharedLink.setError(errorLog);
			} catch (SharedLinkNoCode e) {
				e.printStackTrace();
				SharedLinkErrorLog errorLog = new SharedLinkErrorLog(SharedLinkFetchError.LINK_NO_CODE, new ErrorLog(e.getMessage(), new Date()));
				sharedLink.setError(errorLog);
			} catch (Exception e) {
				e.printStackTrace();
				SharedLinkErrorLog errorLog = new SharedLinkErrorLog(SharedLinkFetchError.GENERAL_ERROR, new ErrorLog(e.getMessage(), new Date()));
				sharedLink.setError(errorLog);
			}
			sharedLinkRepository.save(sharedLink);
			System.out.println(i);
			i++;
		}
	}

	@Transactional
	public List<SharedLink> saveFileSharedLinks() {
		Pattern pattern = Pattern.compile(Constants.regexOpenAiRegex);
		int perPage = 100;
		ObjectMapper objectMapper = new ObjectMapper();
		List<String> aliases = FileUtils.getProgrammingLanguagesAliasGithub();
		List<SharedLink> sharedLinks = new ArrayList<>();
		for (String language: aliases) {
			int page = 1;
			int indexPage = 1;
			SharedLinkSearch search = new SharedLinkSearch();
			while(indexPage <= page) {
				String[] command = {"curl", "-H", "Accept: application/vnd.github.text-match+json", "-H", "Authorization: Bearer "+token, 
						Constants.githubApiBaseUrl+"/search/code?q=https://chat.openai.com/share/+language:"+language+"&page="+indexPage+"&per_page="+perPage};
				String commandJoined = String.join(" ", command);
				try {
					String content = GitHubCall.searchCall(command);
					JsonNode rootNode = objectMapper.readTree(content);
					if(rootNode.get("total_count") != null) {
						int totalCount = rootNode.get("total_count").asInt();
						search.setTotalNumberOfItems(totalCount);
						search.setSearchType(SharedLinkSourceType.FILE);
						search.setSearchCall(commandJoined);
						sharedLinkSearchRepository.save(search);
						if(totalCount > perPage) {
							page = totalCount/perPage;
							if(totalCount%perPage != 0) {
								page = page + 1;
							}
						}
						if(rootNode.get("items") != null && rootNode.get("items").size() > 0) {
							for (JsonNode item : rootNode.get("items")) {
								try {
									File file = new File();
									file.setName(item.get("name").asText());
									file.setSha(item.get("sha").asText());
									file.setPath(item.get("path").asText());
									file.setUrl(item.get("url").asText());
									file.setGitUrl(item.get("git_url").asText());
									file.setHtmlUrl(item.get("html_url").asText());
									file.setLanguage(language);	
									fileRepository.save(file);
									String repoFullName = item.get("repository").get("full_name").asText();
									GitRepository gitRepository = gitRepositoryRepository.findByFullName(repoFullName);
									if(gitRepository == null) {
										gitRepository = new GitRepository(item.get("repository").get("name").asText(), 
												item.get("repository").get("full_name").asText());
										gitRepositoryRepository.save(gitRepository);
									}
									GitRepositoryFile gitRepositoryFile = new GitRepositoryFile(file, gitRepository);
									JsonNode matchesNode = item.get("text_matches");
									if(!matchesNode.isEmpty()) {
										for (JsonNode matchNode : matchesNode) {
											String fragment = matchNode.get("fragment").asText();
											Matcher matcher = pattern.matcher(fragment);
											while(matcher.find()) {
												String link = matcher.group();
												link = link.trim();
												SharedLink sharedLink = sharedLinkRepository.findByLink(link);
												if(sharedLink == null) {
													sharedLink = new SharedLink();
													sharedLink.setLink(link);
													sharedLink.setTextMatchFragment(fragment);
													sharedLink.setType(SharedLinkSourceType.FILE);
													repository.save(sharedLink);
												}
												gitRepositoryFile.getSharedLinks().add(new SharedLinkCommit(sharedLink));
											}
										}
									}
									gitRepositoryFileRepository.save(gitRepositoryFile);
								}catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						indexPage++;
					}else {
						System.out.println("Error searching file citation: "+content);
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return sharedLinks;
	}

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
