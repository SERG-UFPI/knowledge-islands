//package br.com.gitanalyzer.analysis.dev_gpt;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import br.com.gitanalyzer.model.github_openai.CommitCitation;
//import br.com.gitanalyzer.utils.AsyncUtils;
//import br.com.gitanalyzer.utils.Constants;
//
//public class CommitCitationSearch {
//
//	public static List<String> commitFilterDates = Arrays.asList(new String[] {"<2023-10-01", ">2023-10-01"});
//
//	public static List<CommitCitation> getFilesFromCommitsCitations(String token) {
//		try {
//			List<CommitCitation> commitsCitations = getCommitsCitations(token);
//			ExecutorService executorService = AsyncUtils.getExecutorServiceForLogs();
//			List<CompletableFuture<Void>> futures = new ArrayList<>();
//			for (CommitCitation commitCitation : commitsCitations) {
//				CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
//					try {
//						commitCitation.setCommitContent(DevGptSearches.getCommitContent(token, commitCitation.getRepositoryFullName(), commitCitation.getSha()));
//					}catch (Exception e) {
//						System.out.println(e.getMessage());
//					}finally {
//						executorService.shutdown();
//					}
//				}, executorService);
//				futures.add(future);
//			}
//			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//			executorService.shutdown();
//			commitsCitations = filterCommitsCitations(commitsCitations);
//			//			List<CommitCitation> citationsToRemove = new ArrayList<>();
//			//			for (CommitCitation commitCitation : commitsCitations) {
//			//				try {
//			//					boolean remove = false;
//			//					List<CommitCitation> commitsFile = DevGptSearches.getCommitsOfFile(token, commitCitation.getRepositoryFullName(), 
//			//							commitCitation.getCommitContent().getFiles().get(0).getPath());
//			//					List<Contributor> contributorsOfFile = DevGptSearches.getContributorsFromCommits(commitsCitations);
//			//					//					if(commitsFile.size() > 1) {
//			//					//						Contributor contributor1 = new Contributor(commitsFile.get(0).getAuthor().getName(), commitsFile.get(0).getAuthor().getEmail());
//			//					//						for (int i = 1; i < commitsFile.size(); i++) {
//			//					//							Contributor contributor2 = new Contributor(commitsFile.get(i).getAuthor().getName(), commitsFile.get(i).getAuthor().getEmail());
//			//					//							if(!contributor1.getName().equals(contributor2.getName())) {
//			//					//								if(!ProjectVersionExtractor.checkAliasContributors(contributor1, contributor2)) {
//			//					//									remove = true;
//			//					//									break;
//			//					//								}
//			//					//							}
//			//					//						}
//			//					//						if(remove == true) {
//			//					//							citationsToRemove.add(commitCitation);
//			//					//						}
//			//					//					}
//			//
//			//				}catch (Exception e) {
//			//					System.out.println(e.getMessage());
//			//				}
//			//			}
//			//			commitsCitations.removeAll(citationsToRemove);
//			return commitsCitations;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//
////	private static List<CommitCitation> filterCommitsCitations(List<CommitCitation> commitsCitations) {
////		commitsCitations = commitsCitations.stream().filter(c -> !c.getCommitContent().getCommitFiles().isEmpty()).toList();
////		for (CommitCitation commitCitation : commitsCitations) {
////			commitCitation.getCommitContent().setCommitFiles(commitCitation.getCommitContent().getCommitFiles()
////					.stream().filter(f->DevGptSearches.checkProgrammingFileExtension(f.getFileName())).toList());
////		}
////		commitsCitations = commitsCitations.stream().filter(c -> !c.getCommitContent().getCommitFiles().isEmpty()).toList();
////		return commitsCitations;
////	}
//
//	private static List<CommitCitation> getCommitsCitations(String token) throws Exception {
//		Pattern pattern = Pattern.compile(Constants.regexOpenAiRegex);
//		int perPage = 100;
//		ObjectMapper objectMapper = new ObjectMapper();
//		List<CommitCitation> citations = new ArrayList<>();
//		for (String commitDate : commitFilterDates) {
//			int page = 1;
//			int indexPage = 1;
//			while(indexPage <= page && indexPage <= 10) {
//				String[] command = {"curl", "-H", "Accept: application/vnd.github.text-match+json", "-H", "Authorization: Bearer "+token,
//						"https://api.github.com/search/commits?q=https://chat.openai.com/share/+committer-date:"+commitDate+"&page="+indexPage+"&per_page="+perPage};
//				String content = GitHubCall.searchCall(command);
//				JsonNode rootNode = objectMapper.readTree(content);
//				if(rootNode.get("total_count") != null) {
//					int totalCount = rootNode.get("total_count").asInt();
//					if(totalCount > perPage) {
//						page = totalCount/perPage;
//						if(totalCount%perPage != 0) {
//							page = page + 1;
//						}
//					}
//					if(rootNode.get("items") != null && rootNode.get("items").size() > 0) {
//						for (JsonNode item : rootNode.get("items")) {
//							String url = item.get("url").asText();
//							String sha = item.get("sha").asText();
//							String nodeId = item.get("node_id").asText();
//							String htmlUrl = item.get("html_url").asText();
//							String commentsUrl = item.get("comments_url").asText();
//							String authorDate = item.get("commit").get("author").get("date").asText();
//							String authorName = item.get("commit").get("author").get("name").asText();
//							String authorEmail = item.get("commit").get("author").get("email").asText();
//							String commiterDate = item.get("commit").get("committer").get("date").asText();
//							String commiterName = item.get("commit").get("committer").get("name").asText();
//							String commiterEmail = item.get("commit").get("committer").get("email").asText();
//							String message = item.get("commit").get("message").asText();
//							String repositoryFullName = item.get("repository").get("full_name").asText();
//							boolean repositoryPrivate = item.get("repository").get("private").asBoolean();
//							JsonNode matchesNode = item.get("text_matches");
//							List<String> textMatchesFragments = new ArrayList<String>();
//							boolean containsUrl = false;
//							if(!matchesNode.isEmpty()) {
//								for (JsonNode matchNode : matchesNode) {
//									String fragment = matchNode.get("fragment").asText();
//									if(containsUrl == false) {
//										Matcher matcher = pattern.matcher(fragment);
//										while(matcher.find()) {
//											if(matcher.group() != null) {
//												containsUrl = true;
//												break;
//											}
//										}
//									}
//									textMatchesFragments.add(fragment);
//								}
//							}
//							if(containsUrl == true) {
////								citations.add(new CommitCitation(url, sha, nodeId, htmlUrl, commentsUrl, authorDate, authorName, 
////										authorEmail, commiterDate, commiterName, commiterEmail, message, 
////										repositoryFullName, repositoryPrivate, textMatchesFragments));
//							}
//						}
//					}
//					indexPage++;
//				}else {
//					throw new Exception("Error searching file: "+content);
//				}
//			}
//		}
//		return citations;
//	}
//}
