package br.com.gitanalyzer.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gitanalyzer.analysis.dev_gpt.DevGptSearches;
import br.com.gitanalyzer.analysis.dev_gpt.GitHubCall;
import br.com.gitanalyzer.extractors.CommitFileExtractor;
import br.com.gitanalyzer.model.entity.ChatgptConversation;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.entity.SharedLink;
import br.com.gitanalyzer.model.github_openai.FileLinkAuthor;
import br.com.gitanalyzer.model.github_openai.enums.SharedLinkSourceType;
import br.com.gitanalyzer.repository.FileLinkAuthorRepository;
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
	private FileLinkAuthorRepository fileLinkAuthorRepository;
	@Autowired
	private GitRepositoryRepository gitRepositoryRepository;
	@Value("${configuration.github.token}")
	private String token;
	private CommitFileExtractor commitFileExtractor = new CommitFileExtractor();

	public List<SharedLink> saveFileSharedLinkFull() throws Exception{
		List<GitRepository> gitRepositories = new ArrayList<>(); 
		List<SharedLink> sharedLinks = saveFileSharedLinks(token);
		for (SharedLink sharedLink : sharedLinks) {
			if(!gitRepositories.stream().anyMatch(g -> g.getFullName().equals(sharedLink.getRepository().getFullName()))) {
				gitRepositories.add(sharedLink.getRepository());
			}
		}
		saveGitRepositoriesFromApi(gitRepositories);
		return sharedLinks;
	}

	public List<SharedLink> saveFileSharedLink() throws Exception{
		return saveFileSharedLinks(token);
	}

	public List<GitRepository> saveReposFileSharedLinks() throws InterruptedException, IOException {
		List<GitRepository> gitRepositories = new ArrayList<>(); 
		List<SharedLink> sharedLinks = repository.findAll();
		for (SharedLink sharedLink : sharedLinks) {
			if(!gitRepositories.stream().anyMatch(g -> g.getFullName().equals(sharedLink.getRepository().getFullName()))) {
				gitRepositories.add(sharedLink.getRepository());
			}
		}
		saveGitRepositoriesFromApi(gitRepositories);
		return gitRepositories;
	}

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
	public List<SharedLink> saveFileSharedLinks(String token) throws Exception {
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
					Constants.githubApiBaseUrl+"/search/code?q=https://chat.openai.com/share/+language:"+"typescript"+"&page="+indexPage+"&per_page="+perPage};
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
					ExecutorService executorService = AsyncUtils.getExecutorServiceMax();
					List<CompletableFuture<Void>> futures = new ArrayList<>();
					for (JsonNode item : rootNode.get("items")) {
						CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
							try {
								List<String> links = new ArrayList<>();
								FileLinkAuthor fileLinkAuthor = new FileLinkAuthor();
								fileLinkAuthor.getAuthorFile().setAuthorVersion(null);
								fileLinkAuthor.getAuthorFile().getFileVersion().getFile().setName(item.get("name").asText());
								fileLinkAuthor.getAuthorFile().getFileVersion().getFile().setSha(item.get("sha").asText());
								fileLinkAuthor.getAuthorFile().getFileVersion().getFile().setPath(item.get("path").asText());
								fileLinkAuthor.getAuthorFile().getFileVersion().getFile().setUrl(item.get("url").asText());
								fileLinkAuthor.getAuthorFile().getFileVersion().getFile().setGitUrl(item.get("git_url").asText());
								fileLinkAuthor.getAuthorFile().getFileVersion().getFile().setHtmlUrl(item.get("html_url").asText());
								String repoFullName = item.get("repository").get("full_name").asText();
								JsonNode matchesNode = item.get("text_matches");
								if(!matchesNode.isEmpty()) {
									for (JsonNode matchNode : matchesNode) {
										String fragment = matchNode.get("fragment").asText();
										Matcher matcher = pattern.matcher(fragment);
										while(matcher.find()) {
											String link = matcher.group();
											link = link.trim();
											boolean savedLink = fileLinkAuthorRepository.existsBySharedLinkLinkAndSharedLinkRepositoryFullNameAndAuthorFileFileVersionFileName
													(link, repoFullName, fileLinkAuthor.getAuthorFile().getFileVersion().getFile().getName());
											if(!savedLink && !links.contains(link) && link != null) {
												links.add(link);
												try {
													String json = DevGptSearches.getOpenAiJson(link);
													if(json == null) {
														System.out.println();
													}
													ChatgptConversation conversation = DevGptSearches.getConversationOfOpenAiJson(json);
													if (conversation.getConversationTurns().size() == 0) {
														continue;
													}
													if(conversation.getConversationTurns().stream().anyMatch(c -> c.getCodes() != null && c.getCodes().size() > 0) == false) {
														continue;
													}
													SharedLink sharedLink = new SharedLink();
													sharedLink.setLink(link);
													sharedLink.setConversation(conversation);
													sharedLink.setOpenAiFullJson(json);
													sharedLink.setTextMatchFragment(fragment);
													sharedLink.setType(SharedLinkSourceType.FILE);
													GitRepository gitRepository = gitRepositoryRepository.findByFullName(repoFullName);
													if(gitRepository == null) {
														gitRepository = new GitRepository(item.get("repository").get("name").asText(), 
																item.get("repository").get("full_name").asText());
														gitRepositoryRepository.save(gitRepository);
													}
													sharedLink.setRepository(gitRepository);
													repository.save(sharedLink);
													fileLinkAuthor.setSharedLink(sharedLink);
													fileLinkAuthorRepository.save(fileLinkAuthor);
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

	public String setCommitsOfFilesSharedLinks() throws NoHeadException, IOException, GitAPIException {
		List<SharedLink> sharedLinks = repository.findAll();
		for (SharedLink sharedLink : sharedLinks) {
			commitFileExtractor.getCommitFileFromFile(sharedLink);
		}
		return null;
	}

}
