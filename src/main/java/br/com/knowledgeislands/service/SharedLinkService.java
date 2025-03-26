package br.com.knowledgeislands.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.knowledgeislands.analysis.dev_gpt.GitHubCall;
import br.com.knowledgeislands.exceptions.CommandExecutionException;
import br.com.knowledgeislands.exceptions.FetchPageException;
import br.com.knowledgeislands.exceptions.PageJsonProcessingException;
import br.com.knowledgeislands.exceptions.SharedLinkNoCode;
import br.com.knowledgeislands.exceptions.SharedLinkNoConversation;
import br.com.knowledgeislands.exceptions.SharedLinkNotFoundException;
import br.com.knowledgeislands.model.entity.ChatGptConversation;
import br.com.knowledgeislands.model.entity.ConversationTurn;
import br.com.knowledgeislands.model.entity.ErrorLog;
import br.com.knowledgeislands.model.entity.File;
import br.com.knowledgeislands.model.entity.FileRepositorySharedLinkCommit;
import br.com.knowledgeislands.model.entity.GitRepository;
import br.com.knowledgeislands.model.entity.SharedLink;
import br.com.knowledgeislands.model.entity.SharedLinkCommit;
import br.com.knowledgeislands.model.entity.SharedLinkErrorLog;
import br.com.knowledgeislands.model.entity.SharedLinkSearch;
import br.com.knowledgeislands.model.enums.SharedLinkFetchError;
import br.com.knowledgeislands.model.enums.SharedLinkSourceType;
import br.com.knowledgeislands.repository.FileRepository;
import br.com.knowledgeislands.repository.FileRepositorySharedLinkCommitRepository;
import br.com.knowledgeislands.repository.GitRepositoryRepository;
import br.com.knowledgeislands.repository.SharedLinkRepository;
import br.com.knowledgeislands.repository.SharedLinkSearchRepository;
import br.com.knowledgeislands.utils.KnowledgeIslandsUtils;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class SharedLinkService {

	@Autowired
	private SharedLinkRepository repository;
	@Autowired
	private GitRepositoryRepository gitRepositoryRepository;
	@Autowired
	private FileRepository fileRepository;
	@Autowired
	private SharedLinkRepository sharedLinkRepository;
	@Autowired
	private SharedLinkSearchRepository sharedLinkSearchRepository;
	@Autowired
	private FileRepositorySharedLinkCommitRepository fileGitRepositorySharedLinkCommitRepository;
	@Autowired
	private ChatGPTConversationService chatGPTConversationService;
	@Value("${configuration.github.token}")
	private String token;

	public String extractOpenAiJson(String url) throws CommandExecutionException, PageJsonProcessingException, SharedLinkNotFoundException, FetchPageException {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String cookie = System.getenv("OPENAI_COOKIE");
			String[] command = {"curl", "-L", "-b", cookie, url};

			ProcessBuilder processBuilder = new ProcessBuilder(command);
			Process process = processBuilder.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			StringBuilder content = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				content.append(line);
			}

			process.waitFor();
			String html = content.toString();
			int startIndex = html.indexOf(KnowledgeIslandsUtils.openAiJsonStart);
			int endIndex = html.indexOf(KnowledgeIslandsUtils.openAiJsonEnd);
			if(startIndex != -1 && endIndex != -1) {
				String json = html.substring(startIndex + KnowledgeIslandsUtils.openAiJsonStart.length(), endIndex);
				JsonNode rootNode = objectMapper.readTree(json);
				JsonNode errorNode = rootNode.path("state").path("errors").path("root");
				if(!errorNode.isMissingNode() && errorNode.path("status").asInt() == KnowledgeIslandsUtils.pageNotFoundCode) {
					throw new SharedLinkNotFoundException(url);
				}
				return json;
			}else {
				throw new FetchPageException(url);
			}
		}catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new PageJsonProcessingException(e.getMessage());
		}catch (IOException | InterruptedException e) {
			e.printStackTrace();
			throw new CommandExecutionException(e.getMessage());
		}
	}

	@Transactional
	public List<GitRepository> saveGitRepositoriesApi() throws InterruptedException, IOException{
		List<GitRepository> repositories = fileGitRepositorySharedLinkCommitRepository.findDistinctGitRepositoriesWithNonNullConversation();
		ObjectMapper objectMapper = new ObjectMapper();
		ExecutorService executorService = KnowledgeIslandsUtils.getExecutorServiceMax();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (GitRepository gitRepository : repositories) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
				try {
					if(gitRepository.getCloneUrl() == null) {
						String[] command = {"curl", "-H", "Authorization: Bearer "+token, 
								KnowledgeIslandsUtils.githubApiBaseUrl+"/repos/"+gitRepository.getFullName()};
						String content = GitHubCall.generalCall(command);
						JsonNode rootNode = objectMapper.readTree(content);
						JsonNode privateNode = rootNode.get("private");
						gitRepository.setPrivateRepository(privateNode!=null?privateNode.asBoolean():false);
						gitRepository.setCloneUrl(rootNode.get("clone_url")!=null?rootNode.get("clone_url").asText():null);
						if(gitRepository.getCloneUrl() == null) {
							gitRepository.setCloneUrl(KnowledgeIslandsUtils.gitHubBaseUrl+gitRepository.getFullName());
						}
						gitRepository.setLanguage(rootNode.get("language")!=null?rootNode.get("language").asText():null);
						gitRepository.setDefaultBranch(rootNode.get("default_branch")!=null?rootNode.get("default_branch").asText():null);
						gitRepository.setSize(rootNode.get("size")!=null?rootNode.get("size").asInt():0);
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
				String json = extractOpenAiJson(sharedLink.getLink());
				ChatGptConversation conversation = chatGPTConversationService.getConversationOfOpenAiJson(json);
				if (conversation.getConversationTurns().isEmpty()) {
					throw new SharedLinkNoConversation(sharedLink.getLink());
				}
				for(ConversationTurn conversationTurn: conversation.getConversationTurns()) {
					if(conversationTurn.getCodes() != null && !conversationTurn.getCodes().isEmpty()) {
						conversationTurn.setCodes(conversationTurn.getCodes().stream().filter(c -> c.getLanguage()!=null).toList());
					}
				}
				if(conversation.getConversationTurns().stream().noneMatch(c -> c.getCodes() != null && !c.getCodes().isEmpty())) {
					throw new SharedLinkNoCode(sharedLink.getLink());
				}
				sharedLink.setConversation(conversation);
			}catch (SharedLinkNotFoundException e) {
				e.printStackTrace();
				SharedLinkErrorLog errorLog = new SharedLinkErrorLog(SharedLinkFetchError.NOT_FOUND, new ErrorLog(e.getMessage(), new Date()));
				sharedLink.setError(errorLog);
			} catch (FetchPageException e) {
				SharedLinkErrorLog errorLog = new SharedLinkErrorLog(SharedLinkFetchError.FETCH_ERROR, new ErrorLog(e.getMessage(), new Date()));
				sharedLink.setError(errorLog);
			} catch (PageJsonProcessingException e) {
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
			} catch (CommandExecutionException e) {
				e.printStackTrace();
				SharedLinkErrorLog errorLog = new SharedLinkErrorLog(SharedLinkFetchError.COMMAND_IO_EXCEPTION, new ErrorLog(e.getMessage(), new Date()));
				sharedLink.setError(errorLog);
			} catch (Exception e) {
				e.printStackTrace();
				SharedLinkErrorLog errorLog = new SharedLinkErrorLog(SharedLinkFetchError.GENERAL_ERROR, new ErrorLog(e.getMessage(), new Date()));
				sharedLink.setError(errorLog);
			}
			sharedLinkRepository.save(sharedLink);
			log.info(i);
			i++;
		}
	}

	private void saveFileSharedLinkItem(JsonNode item, String language) {
		try {
			Pattern pattern = Pattern.compile(KnowledgeIslandsUtils.regexOpenAiRegexChat);
			Pattern pattern2 = Pattern.compile(KnowledgeIslandsUtils.regexOpenAiRegexChatGPT);
			String repositoryLabel = "repository";
			JsonNode repositoryNode = item.get(repositoryLabel);
			if(repositoryNode.has("full_name")) {
				String repoFullName = repositoryNode.get("full_name").asText();
				List<String> problematicProjects = KnowledgeIslandsUtils.problematicGenAiProject();
				if(!problematicProjects.contains(repoFullName)) {
					GitRepository gitRepository = gitRepositoryRepository.findByFullName(repoFullName);
					if(gitRepository == null) {
						gitRepository = new GitRepository(repositoryNode.get("name").asText(), 
								repositoryNode.get("full_name").asText(), repositoryNode.get("private").asBoolean());
						gitRepositoryRepository.save(gitRepository);
					}
					String fileUrl = item.get("url").asText();
					File file = fileRepository.findByUrl(fileUrl);
					if(file == null) {
						file = new File();
						file.setName(item.get("name").asText());
						file.setPath(item.get("path").asText());
						if(KnowledgeIslandsUtils.containsNonAscii(file.getPath())) {
							file.setPath(KnowledgeIslandsUtils.encodeNonAsciiOnly(file.getPath()));
						}
						file.setSha(item.get("sha").asText());
						file.setUrl(fileUrl);
						file.setGitUrl(item.get("git_url").asText());
						file.setHtmlUrl(item.get("html_url").asText());
						file.setLanguage(language);
						fileRepository.save(file);
					}
					FileRepositorySharedLinkCommit fileGitRepositorySharedLinkCommit = fileGitRepositorySharedLinkCommitRepository
							.findByFileIdAndGitRepositoryId(file.getId(), gitRepository.getId());
					if(fileGitRepositorySharedLinkCommit == null) {
						fileGitRepositorySharedLinkCommit = new FileRepositorySharedLinkCommit(file, gitRepository);
					}
					JsonNode matchesNode = item.get("text_matches");
					if(!matchesNode.isEmpty()) {
						for (JsonNode matchNode : matchesNode) {
							String fragment = matchNode.get("fragment").asText();
							Matcher matcher = pattern.matcher(fragment);
							while(matcher.find()) {
								addSharedLinkCommit(matcher, fragment, fileGitRepositorySharedLinkCommit);
							}
							Matcher matcher2 = pattern2.matcher(fragment);
							while(matcher2.find()) {
								addSharedLinkCommit(matcher2, fragment, fileGitRepositorySharedLinkCommit);
							}
						}
					}
					if(fileGitRepositorySharedLinkCommit.getSharedLinksCommits() != null && !fileGitRepositorySharedLinkCommit.getSharedLinksCommits().isEmpty()) {
						fileGitRepositorySharedLinkCommitRepository.save(fileGitRepositorySharedLinkCommit);
					}
				}
			}else {
				log.error("Missing repository information in item: " + item.toString());
			}
		}catch (Exception e) {
			log.error(e);
		}
	}

	private void addSharedLinkCommit(Matcher matcher, String fragment, FileRepositorySharedLinkCommit fileGitRepositorySharedLinkCommit) {
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
		boolean addSharedLinkCommit = true;
		for(SharedLinkCommit slc: fileGitRepositorySharedLinkCommit.getSharedLinksCommits()) {
			if(slc.getSharedLink().getLink().equals(sharedLink.getLink())) {
				addSharedLinkCommit = false;
				break;
			}
		}
		if(addSharedLinkCommit) {
			fileGitRepositorySharedLinkCommit.getSharedLinksCommits().add(new SharedLinkCommit(sharedLink, fileGitRepositorySharedLinkCommit));
		}
	}

	@Transactional
	public void saveFileSharedLinks(String searchTerm) {
		String itemsLabel = "items";
		int perPage = 100;
		ObjectMapper objectMapper = new ObjectMapper();
		List<String> aliases = KnowledgeIslandsUtils.getProgrammingLanguagesAliasGithub();
		for (String language: aliases) {
			int page = 1;
			int indexPage = 1;
			SharedLinkSearch search = new SharedLinkSearch();
			while(indexPage <= page) {
				String[] command = {"curl", "-H", "Accept: application/vnd.github.text-match+json", "-H", "Authorization: Bearer "+token, 
						KnowledgeIslandsUtils.githubApiBaseUrl+"/search/code?q="+searchTerm+"+language:"+language+"&page="+indexPage+"&per_page="+perPage};
				String commandJoined = String.join(" ", command);
				try {
					String content = GitHubCall.searchCall(command);
					JsonNode rootNode = objectMapper.readTree(content);
					if(rootNode.has("total_count") && rootNode.get("total_count") != null) {
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
						if(rootNode.get(itemsLabel) != null && rootNode.get(itemsLabel).size() > 0) {
							JsonNode items = rootNode.get(itemsLabel);
							for (JsonNode item : items) {
								saveFileSharedLinkItem(item, language);
							}
						}
						indexPage++;
					}else {
						log.error("Error searching file citation: "+content);
					}
				}catch (Exception e) {
					e.printStackTrace();
					log.error(e.getMessage());
				}
			}
		}
	}

	@Transactional
	public Object numberSharedLinksPerLanguage() {
		//		int sum = 0;
		//		List<String> languages = fileRepository.findDistinctLanguages();
		//		for (String language : languages) {
		//			System.out.println("========= "+language+" =========");
		//			List<String> links = new ArrayList<>();
		//			List<GitRepositoryFile> gitRepositoryFiles = gitRepositoryFileRepository.findByFileLanguage(language);
		//			int numFiles = 0;
		//			for (GitRepositoryFile gitRepositoryFile : gitRepositoryFiles) {
		//				boolean possuiSharedLink = false;
		//				for (SharedLinkCommit sharedLinkCommit : gitRepositoryFile.getSharedLinks()) {
		//					if(!links.contains(sharedLinkCommit.getSharedLink().getLink()) && sharedLinkCommit.getSharedLink().getConversation() != null) {
		//						links.add(sharedLinkCommit.getSharedLink().getLink());
		//						possuiSharedLink = true;
		//					}
		//				}
		//				if(possuiSharedLink == true) {
		//					numFiles++;
		//				}
		//			}
		//			System.out.println("Number of files: "+numFiles);
		//			System.out.println("Number of shared links: "+links.size());
		//			sum = sum+links.size();
		//			System.out.println();
		//		}
		//		System.out.println(sum);

		//		List<Integer> distributionOfFiles = new ArrayList<>();
		//		List<Integer> distributionOfSharedLinks = new ArrayList<>();
		//		List<GitRepositoryFile> files = gitRepositoryFileRepository.findAll();
		//		List<GitRepository> repos = gitRepositoryRepository.findAll();
		//		for(GitRepository repo: repos) {
		//			List<GitRepositoryFile> filesRepo = files.stream().filter(f -> f.getGitRepository().getId().equals(repo.getId())).toList();
		//			int numValidFiles = 0;
		//			for (GitRepositoryFile file : filesRepo) {
		//				int numValidLinks = 0;
		//				boolean validFile = false;
		//				for (SharedLinkCommit sharedLinkCommit : file.getSharedLinks()) {
		//					if(sharedLinkCommit.getSharedLink().getConversation() != null) {
		//						validFile = true;
		//						numValidLinks++;
		//					}
		//				}
		//				if(validFile == true) {
		//					distributionOfSharedLinks.add(numValidLinks);
		//					numValidFiles++;
		//				}
		//			}
		//			if(numValidFiles > 0) {
		//				distributionOfFiles.add(numValidFiles);
		//			}
		//		}
		//		log.info("=== Distribution stats of files per repo ===");
		//		getStatistcsOfList(distributionOfFiles);
		//		log.info("=== Distribution stats of shared links per repo===");
		//		getStatistcsOfList(distributionOfSharedLinks);
		return null;
	}

	public void createSharedLinkConversationRepo() throws InterruptedException, IOException {
		for (String term : KnowledgeIslandsUtils.getChatGPTSearchTerms()) {
			saveFileSharedLinks(term);
		}
		setConversationSharedLinks();
		saveGitRepositoriesApi();
	}

}
