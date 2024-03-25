package br.com.gitanalyzer.analysis.dev_gpt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gitanalyzer.model.github_openai.FileCitation;
import br.com.gitanalyzer.utils.AsyncUtils;
import br.com.gitanalyzer.utils.Constants;
import br.com.gitanalyzer.utils.FileUtils;

public class FileCitationSearch {

	public static List<FileCitation> getFilesFromFilesCitations(String token) {
		try {
			List<FileCitation> filesCitations = getFilesCitations(token);
			ExecutorService executorService = AsyncUtils.getExecutorServiceForLogs();
			List<CompletableFuture<Void>> futures = new ArrayList<>();
			for (FileCitation fileCitation : filesCitations) {
				CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
					try {
						fileCitation.getFileDoe().setFile(DevGptSearches.getFileContent(token, fileCitation.getRepositoryFullName(), fileCitation.getFileDoe().getFile().getPath()));
					}catch (Exception e) {
						System.out.println(e.getMessage());
					}finally {
						executorService.shutdown();
					}
				}, executorService);
				futures.add(future);
			}
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
			executorService.shutdown();
			filesCitations = filesCitations.stream().filter(f -> f.getFileDoe().getFile() != null).toList();
			ExecutorService executorService2 = AsyncUtils.getExecutorServiceForLogs();
			List<CompletableFuture<Void>> futures2 = new ArrayList<>();
			for (FileCitation fileCitation : filesCitations) {
				CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
					try {
						fileCitation.getFileDoe().getFile().setCommits(DevGptSearches.getCommitsOfFile(token, fileCitation.getRepositoryFullName(), 
								fileCitation.getFileDoe().getFile().getPath()));
					}catch (Exception e) {
						System.out.println(e.getMessage());
					}finally {
						executorService2.shutdown();
					}
				}, executorService2);
				futures2.add(future);
			}
			CompletableFuture.allOf(futures2.toArray(new CompletableFuture[0])).join();
			executorService2.shutdown();
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
		//for (int i = 0; i < aliases.size(); i++) {
		int page = 1;
		int indexPage = 1;
		while(indexPage <= page) {
			String[] command = {"curl", "-H", "Accept: application/vnd.github.text-match+json", "-H", "Authorization: Bearer "+token, 
					"https://api.github.com/search/code?q=https://chat.openai.com/share/+language:"+aliases.get(0)+"&page="+indexPage+"&per_page="+perPage};
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
					for (JsonNode item : rootNode.get("items")) {
						String name = item.get("name").asText();
						String path = item.get("path").asText();
						String sha = item.get("sha").asText();
						String url = item.get("url").asText();
						String gitUrl = item.get("git_url").asText();
						String htmlUrl = item.get("html_url").asText();
						String repositoryFullName = item.get("repository").get("full_name").asText();
						JsonNode matchesNode = item.get("text_matches");
						List<String> textMatchesFragments = new ArrayList<String>();
						boolean containsUrl = false;
						if(!matchesNode.isEmpty()) {
							for (JsonNode matchNode : matchesNode) {
								String fragment = matchNode.get("fragment").asText();
								if(containsUrl == false) {
									Matcher matcher = pattern.matcher(fragment);
									while(matcher.find()) {
										if(matcher.group() != null) {
											containsUrl = true;
										}
									}
								}
								textMatchesFragments.add(matchNode.get("fragment").asText());
							}
						}
						if(containsUrl == true) {
							FileCitation fileCitation = new FileCitation();
							fileCitation.getFileDoe().getFile().setSha(sha);
							fileCitation.getFileDoe().getFile().setPath(path);
							fileCitation.getFileDoe().getFile().setUrl(url);
							fileCitation.getFileDoe().getFile().setGitUrl(gitUrl);
							fileCitation.getFileDoe().getFile().setHtmlUrl(htmlUrl);
							fileCitation.setRepositoryFullName(repositoryFullName);
							fileCitation.setTextMatchesFragments(textMatchesFragments);
							citations.add(fileCitation);
						}
					}
				}
				indexPage++;
			}else {
				throw new Exception("Error searching file: "+content);
			}
		}
		//}
		return citations;
	}
}
