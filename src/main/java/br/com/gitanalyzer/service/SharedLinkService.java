package br.com.gitanalyzer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gitanalyzer.analysis.dev_gpt.DevGptSearches;
import br.com.gitanalyzer.analysis.dev_gpt.GitHubCall;
import br.com.gitanalyzer.model.entity.ChatgptConversation;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.entity.SharedLink;
import br.com.gitanalyzer.model.github_openai.FileLinkAuthor;
import br.com.gitanalyzer.model.github_openai.enums.SharedLinkSourceType;
import br.com.gitanalyzer.repository.GitRepositoryRepository;
import br.com.gitanalyzer.repository.SharedLinkRepository;
import br.com.gitanalyzer.utils.AsyncUtils;
import br.com.gitanalyzer.utils.Constants;
import br.com.gitanalyzer.utils.FileUtils;

@Service
public class SharedLinkService {

	@Autowired
	private SharedLinkRepository repository;
	@Autowired
	private GitRepositoryRepository gitRepositoryRepository;
	@Value("${configuration.github.token}")
	private String token;

	public List<SharedLink> getFileSharedLink() throws Exception{
		List<GitRepository> gitRepositories = new ArrayList<>(); 
		List<SharedLink> sharedLinks = getFileSharedLinks(token);
		for (SharedLink sharedLink : sharedLinks) {
			if(!gitRepositories.stream().anyMatch(g -> g.getFullName().equals(sharedLink.getRepository().getFullName()))) {
				gitRepositories.add(sharedLink.getRepository());
			}
		}
		gitRepositories = getGitRepositoriesFromApi(gitRepositories);
		gitRepositoryRepository.saveAll(gitRepositories);
		for (SharedLink sharedLink : sharedLinks) {
			sharedLink.setRepository(gitRepositories.stream().filter(g -> g.getFullName().equals(sharedLink.getRepository().getFullName())).findFirst().get());
		}
		repository.saveAll(sharedLinks);
		return sharedLinks;
	}
	
	public List<GitRepository> getGitRepositoriesFromApi(List<GitRepository> repositories){
		ObjectMapper objectMapper = new ObjectMapper();
		ExecutorService executorService = AsyncUtils.getExecutorServiceForLogs();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (GitRepository gitRepository : repositories) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
				try {
					String[] command = {"curl", "-H", "Authorization: Bearer "+token, 
							Constants.githubApiBaseUrl+"/repos/"+gitRepository.getFullName()};
					String content = GitHubCall.searchCall(command);
					JsonNode rootNode = objectMapper.readTree(content);
					gitRepository.setPrivateRepository(rootNode.get("private").asBoolean());
					gitRepository.setCloneUrl(rootNode.get("clone_url").asText());
					gitRepository.setLanguage(rootNode.get("language").asText());
					gitRepository.setDefaultBranch(rootNode.get("default_branch").asText());
					System.out.println();
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

	public static List<SharedLink> getFileSharedLinks(String token) throws Exception {
		Pattern pattern = Pattern.compile(Constants.regexOpenAiRegex);
		int perPage = 100;
		ObjectMapper objectMapper = new ObjectMapper();
		List<String> aliases = FileUtils.getProgrammingLanguagesAliasGithub();
		List<SharedLink> sharedLinks = new ArrayList<>();
		//for (String language: aliases) {
		int page = 1;
		int indexPage = 1;
		while(indexPage <= page) {
			String[] command = {"curl", "-H", "Accept: application/vnd.github.text-match+json", "-H", "Authorization: Bearer "+token, 
					Constants.githubApiBaseUrl+"/search/code?q=https://chat.openai.com/share/+language:"+"csharp"+"&page="+indexPage+"&per_page="+perPage};
			String content = GitHubCall.searchCall(command);
			JsonNode rootNode = objectMapper.readTree(content);
			if(rootNode.get("total_count") != null) {
				int totalCount = rootNode.get("total_count").asInt();
				if(totalCount > perPage) {
					page = totalCount/perPage;
					if(totalCount%perPage != 0) {
						page = page + 1;
					}
				}
				if(rootNode.get("items") != null && rootNode.get("items").size() > 0) {
					ExecutorService executorService = AsyncUtils.getExecutorServiceForLogs();
					List<CompletableFuture<Void>> futures = new ArrayList<>();
					for (JsonNode item : rootNode.get("items")) {
						CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
							try {
								List<String> links = new ArrayList<>();
								FileLinkAuthor fileLinkAuthor = new FileLinkAuthor();
								fileLinkAuthor.getAuthorFile().getFile().setName(item.get("name").asText());
								fileLinkAuthor.getAuthorFile().getFile().setSha(item.get("sha").asText());
								fileLinkAuthor.getAuthorFile().getFile().setPath(item.get("path").asText());
								fileLinkAuthor.getAuthorFile().getFile().setUrl(item.get("url").asText());
								fileLinkAuthor.getAuthorFile().getFile().setGitUrl(item.get("git_url").asText());
								fileLinkAuthor.getAuthorFile().getFile().setHtmlUrl(item.get("html_url").asText());
								GitRepository repository = new GitRepository(item.get("repository").get("name").asText(), 
										item.get("repository").get("full_name").asText());
								fileLinkAuthor.getAuthorFile().getFile().setRepository(repository);
								JsonNode matchesNode = item.get("text_matches");
								if(!matchesNode.isEmpty()) {
									for (JsonNode matchNode : matchesNode) {
										String fragment = matchNode.get("fragment").asText();
										Matcher matcher = pattern.matcher(fragment);
										while(matcher.find()) {
											String link = matcher.group();
											link = link.trim();
											if(!links.contains(link) && link != null) {
												links.add(link);
												try {
													String json = DevGptSearches.getOpenAiJson(link);
													ChatgptConversation conversation = DevGptSearches.getConversationOfOpenAiJson(json);
													if (conversation.getConversationTurns().size() == 0) {
														continue;
													}
													if(conversation.getConversationTurns().stream().anyMatch(c -> c.getCodes() != null && c.getCodes().size() > 0) == false) {
														continue;
													}
													SharedLink sharedLink = new SharedLink();
													sharedLink.getFilesLinkAuthor().add(fileLinkAuthor);
													sharedLink.setLink(link);
													sharedLink.setConversation(conversation);
													sharedLink.setOpenAiFullJson(json);
													sharedLink.setTextMatchFragment(fragment);
													sharedLink.setType(SharedLinkSourceType.FILE);
													sharedLink.setRepository(repository);
													sharedLinks.add(sharedLink);
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										}
									}
								}
							}catch (Exception e) {
								e.printStackTrace();
							}
						}, executorService);
						futures.add(future);
					}
					CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
					executorService.shutdown();
				}
				indexPage++;
			}else {
				throw new Exception("Error searching file citation: "+content);
			}
		}
		//}
		return sharedLinks;
	}

}
