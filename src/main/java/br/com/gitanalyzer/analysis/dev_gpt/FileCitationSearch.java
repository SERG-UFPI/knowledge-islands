package br.com.gitanalyzer.analysis.dev_gpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gitanalyzer.model.github_openai.ChatgptConversation;
import br.com.gitanalyzer.model.github_openai.ChatgptUserAgent;
import br.com.gitanalyzer.model.github_openai.Commit;
import br.com.gitanalyzer.model.github_openai.CommitFile;
import br.com.gitanalyzer.model.github_openai.ConversationTurn;
import br.com.gitanalyzer.model.github_openai.FileCitation;
import br.com.gitanalyzer.model.github_openai.PromptCode;
import br.com.gitanalyzer.model.github_openai.SharedLink;
import br.com.gitanalyzer.utils.AsyncUtils;
import br.com.gitanalyzer.utils.Constants;
import br.com.gitanalyzer.utils.FileUtils;

public class FileCitationSearch {

	public static List<FileCitation> getFilesFromFilesCitations(String token) {
		try {
			//String json = DevGptSearches.getGithubGraphqlFileBlame("OtavioCury", "knowledge-islands", "main", "src/main/java/br/com/gitanalyzer/api/AuthController.java");
			List<FileCitation> filesCitations = getFilesCitations(token);
			ExecutorService executorService = AsyncUtils.getExecutorServiceForLogs();
			List<CompletableFuture<Void>> futures = new ArrayList<>();
			for (FileCitation fileCitation : filesCitations) {
				CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
					try {
						fileCitation.getFileAuthor().setFile(DevGptSearches.getFileContent(token, fileCitation.getRepositoryFullName(), fileCitation.getFileAuthor().getFile().getPath()));
					}catch (Exception e) {
						e.printStackTrace();
					}
				}, executorService);
				futures.add(future);
			}
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
			executorService.shutdown();
			filesCitations = filesCitations.stream().filter(f -> f.getFileAuthor().getFile() != null).toList();
			ExecutorService executorService2 = AsyncUtils.getExecutorServiceForLogs();
			List<CompletableFuture<Void>> futures2 = new ArrayList<>();
			for (FileCitation fileCitation : filesCitations) {
				CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
					try {
						fileCitation.getFileAuthor().getFile().setCommits(DevGptSearches.getCommitsOfFile(token, fileCitation.getRepositoryFullName(), 
								fileCitation.getFileAuthor().getFile().getPath()));
					}catch (Exception e) {
						e.printStackTrace();
					}
				}, executorService2);
				futures2.add(future);
			}
			CompletableFuture.allOf(futures2.toArray(new CompletableFuture[0])).join();
			executorService2.shutdown();
			filesCitations = filesCitations.stream().filter(f -> f.getFileAuthor().getFile().getCommits() != null && f.getFileAuthor().getFile().getCommits().size() > 0).toList();
			for (FileCitation fileCitation : filesCitations) {
				fileCitation.getFileAuthor().getFile().setCommits(fileCitation.getFileAuthor().getFile().getCommits().stream().sorted(Comparator.comparing(Commit::getAuthorDate)).toList());
			}
			ExecutorService executorService3 = AsyncUtils.getExecutorServiceForLogs();
			List<CompletableFuture<Void>> futures3 = new ArrayList<>();
			for (FileCitation fileCitation : filesCitations) {
				CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
					try {
						for (Commit commit : fileCitation.getFileAuthor().getFile().getCommits()) {
							commit.setCommitFiles(DevGptSearches.getCommitFiles(token, fileCitation.getRepositoryFullName(), commit.getSha()));
						}
						for (SharedLink link: fileCitation.getSharedLinks()) {
							Commit commitThatAddedLink = DevGptSearches.getCommitThatAddedLink(fileCitation.getFileAuthor().getFile(), link.getLink());
							link.setCommitThatAddedTheLink(commitThatAddedLink);
							CommitFile commitFile = link.getCommitThatAddedTheLink().getCommitFiles().stream()
									.filter(cf -> cf.getFilePath().equals(fileCitation.getFileAuthor().getFile().getPath())).findFirst().get();
							List<String> codes = DevGptSearches.getCodesFromConversation(link.getConversation().getConversationTurns());
							link.setLinesCopied(DevGptSearches.getLinesCopied(codes, commitFile.getAddedLines()));
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
				}, executorService3);
				futures3.add(future);
			}
			CompletableFuture.allOf(futures3.toArray(new CompletableFuture[0])).join();
			executorService3.shutdown();
			for (FileCitation fileCitation : filesCitations) {
				fileCitation.setSharedLinks(fileCitation.getSharedLinks().stream().filter(sl -> sl.getCommitThatAddedTheLink() != null).toList());
			}
			filesCitations = filesCitations.stream().filter(fc -> fc.getSharedLinks() != null && fc.getSharedLinks().size() > 0).toList();
			for (FileCitation fileCitation : filesCitations) {
				for (SharedLink sharedLink : fileCitation.getSharedLinks()) {
					List<Commit> commtis = DevGptSearches.getAllCommitsOfAuthor(fileCitation.getFileAuthor().getFile().getCommits(), sharedLink.getCommitThatAddedTheLink().getAuthor());
					System.out.println();
				}
			}
			return filesCitations;
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	public static List<FileCitation> getFilesCitations(String token) throws Exception {
		Pattern pattern = Pattern.compile(Constants.regexOpenAiRegex);
		int perPage = 100;
		ObjectMapper objectMapper = new ObjectMapper();
		List<String> aliases = FileUtils.getProgrammingLanguagesAliasGithub();
		List<FileCitation> citations = new ArrayList<>();
		//for (String language: aliases) {
		int page = 1;
		int indexPage = 1;
		while(indexPage <= page) {
			String[] command = {"curl", "-H", "Accept: application/vnd.github.text-match+json", "-H", "Authorization: Bearer "+token, 
					"https://api.github.com/search/code?q=https://chat.openai.com/share/+language:"+"csharp"+"&page="+indexPage+"&per_page="+perPage};
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
								String name = item.get("name").asText();
								String path = item.get("path").asText();
								String sha = item.get("sha").asText();
								String url = item.get("url").asText();
								String gitUrl = item.get("git_url").asText();
								String htmlUrl = item.get("html_url").asText();
								String repositoryFullName = item.get("repository").get("full_name").asText();
								JsonNode matchesNode = item.get("text_matches");
								List<SharedLink> sharedLinks = new ArrayList<>();
								if(!matchesNode.isEmpty()) {
									for (JsonNode matchNode : matchesNode) {
										String fragment = matchNode.get("fragment").asText();
										Matcher matcher = pattern.matcher(fragment);
										while(matcher.find()) {
											String link = matcher.group();
											link = link.trim();
											boolean contains = false;
											if(sharedLinks.size() > 0) {
												for (SharedLink sharedLink : sharedLinks) {
													if(sharedLink.getLink().equals(link)) {
														contains = true;
													}
												}
											}
											if(link != null && contains == false) {
												try {
													String json = DevGptSearches.getOpenAiJson(link);
													ChatgptConversation conversation = DevGptSearches.getConversationOfOpenAiJson(json);
													if (conversation.getConversationTurns().size() == 0) {
														continue;
													}
													if(conversation.getConversationTurns().stream().anyMatch(c -> c.getCodes() != null && c.getCodes().size() > 0) == false) {
														continue;
													}
													//											boolean hasCodeSnippetLanguage = false;
													//											conversationFor: for (ConversationTurn turn : conversation.getConversationTurns()) {
													//												if(turn.getCodes() != null && turn.getCodes().size() > 0) {
													//													for (PromptCode code : turn.getCodes()) {
													//														if(code.getLanguage().toUpperCase().equals(aliases.get(0).toUpperCase())) {
													//															hasCodeSnippetLanguage = true;
													//															break conversationFor;
													//														}
													//													}
													//												}
													//											}
													//											if(hasCodeSnippetLanguage == false) {
													//												System.out.println("Link without language: "+matched);
													//												continue;
													//											}
													sharedLinks.add(new SharedLink(link, fragment, conversation, json));
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										}
									}
								}
								if(sharedLinks.size() > 0) {
									FileCitation fileCitation = new FileCitation();
									fileCitation.getFileAuthor().getFile().setName(name);
									fileCitation.getFileAuthor().getFile().setSha(sha);
									fileCitation.getFileAuthor().getFile().setPath(path);
									fileCitation.getFileAuthor().getFile().setUrl(url);
									fileCitation.getFileAuthor().getFile().setGitUrl(gitUrl);
									fileCitation.getFileAuthor().getFile().setHtmlUrl(htmlUrl);
									fileCitation.setRepositoryFullName(repositoryFullName);
									fileCitation.setSharedLinks(sharedLinks);
									citations.add(fileCitation);
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
		return citations;
	}
}
