package br.com.gitanalyzer.analysis.dev_gpt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.github_openai.ChatgptConversation;
import br.com.gitanalyzer.model.github_openai.ChatgptUserAgent;
import br.com.gitanalyzer.model.github_openai.Commit;
import br.com.gitanalyzer.model.github_openai.CommitCitation;
import br.com.gitanalyzer.model.github_openai.ConversationTurn;
import br.com.gitanalyzer.model.github_openai.File;
import br.com.gitanalyzer.model.github_openai.FileCitation;
import br.com.gitanalyzer.model.github_openai.PromptCode;
import br.com.gitanalyzer.utils.Constants;
import br.com.gitanalyzer.utils.FileUtils;


public class DevGptSearches {

	public static void main(String[] args) throws IOException, InterruptedException {
		String token = args[0];
		List<FileCitation> fileCitations = FileCitationSearch.getFilesFromFilesCitations(token);
//		List<CommitCitation> commitCitations = CommitCitationSearch.getFilesFromCommitsCitations(token);
//		int numberOfFiles = 1252;
//		for (CommitCitation commitCitation : commitCitations) {
//			numberOfFiles = numberOfFiles+commitCitation.getCommitContent().getFiles().size();
//		}
//		System.out.println();
		//		try {
		//			List<FileCitation> filesCitations = getFilesCitations(token);
		//			ExecutorService executorService = AsyncUtils.getExecutorServiceForLogs();
		//			List<CompletableFuture<Void>> futures = new ArrayList<>();
		//			for (FileCitation fileCitation : filesCitations) {
		//				CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
		//					try {
		//						fileCitation.setFileContent(getFileContent(token, fileCitation.getRepositoryFullName(), fileCitation.getPath()));
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
		//			filesCitations = filesCitations.stream().filter(f -> f.getFileContent() != null).toList();
		//		}catch (Exception e) {
		//			System.out.println(e.getMessage());
		//		}
		String json = getOpenAiJson("https://chat.openai.com/share/3f615ac3-2dd3-4dba-9456-3ff8f5530628");
		//String html = getOpenAiHtmlPageNodeScrape("https://chat.openai.com/share/01252095-f0bc-48f9-8d36-07f14fb0c76d", scrapeFilePath);
		//getElementsOfOpenAiHtml(html);
		ChatgptConversation conversation = getConversationOfOpenAiJson(json);
	}

	public static boolean checkProgrammingFileExtension(String path) {
		String extension = FileUtils.getFileExtension(path);
		List<String> programmingExtesions = FileUtils.getProgrammingExtensions();
		if(programmingExtesions.contains(extension)) {
			return true;
		}else {
			return false;
		}
	}

//	public static List<CommitCitation> getCommitsOfFile(String token, String repoFullName, String filePath) throws Exception{
//		filePath = UriUtils.encodePath(filePath, "UTF-8");
//		List<CommitCitation> citations = new ArrayList<>();
//		ObjectMapper objectMapper = new ObjectMapper();
//		String[] command = {"curl", "-L", "-H", "Accept: application/vnd.github+json", "-H", "Authorization: Bearer "+token, 
//				"https://api.github.com/repos/"+repoFullName+"/commits?path="+filePath}; 
//		String content = GitHubCall.generalCall(command);
//		JsonNode rootNode = objectMapper.readTree(content);
//		if(!rootNode.isEmpty()) {
//			for (JsonNode jsonNode : rootNode) {
//				String url = jsonNode.get("url").asText();
//				String sha = jsonNode.get("sha").asText();
//				String nodeId = jsonNode.get("node_id").asText();
//				String htmlUrl = jsonNode.get("html_url").asText();
//				String authorDate = jsonNode.get("commit").get("author").get("date").asText();
//				String authorName = jsonNode.get("commit").get("author").get("name").asText();
//				String authorEmail = jsonNode.get("commit").get("author").get("email").asText();
//				String commiterDate = jsonNode.get("commit").get("committer").get("date").asText();
//				String commiterName = jsonNode.get("commit").get("committer").get("name").asText();
//				String commiterEmail = jsonNode.get("commit").get("committer").get("email").asText();
////				citations.add(new CommitCitation(url, sha, nodeId, htmlUrl, authorDate, authorName, 
////						authorEmail, commiterDate, commiterName, commiterEmail));
//			}
//			return citations;
//		}else {
//			throw new Exception("Error searching file commits of repo: "+repoFullName+" of file: "+filePath+" ; content: "+content);
//		}
//	}
	
	public static List<Commit> getCommitsOfFile(String token, String repoFullName, String filePath) throws Exception{
		filePath = UriUtils.encodePath(filePath, "UTF-8");
		List<Commit> commits = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		String[] command = {"curl", "-L", "-H", "Accept: application/vnd.github+json", "-H", "Authorization: Bearer "+token, 
				"https://api.github.com/repos/"+repoFullName+"/commits?path="+filePath}; 
		String content = GitHubCall.generalCall(command);
		JsonNode rootNode = objectMapper.readTree(content);
		if(!rootNode.isEmpty()) {
			for (JsonNode jsonNode : rootNode) {
				Commit commit = new Commit();
				commit.setUrl(jsonNode.get("url").asText());
				commit.setSha(jsonNode.get("sha").asText());
				commit.setNodeId(jsonNode.get("node_id").asText());
				commit.setHtmlUrl(jsonNode.get("html_url").asText());
				commit.setAuthorDate(getDateFromString(jsonNode.get("commit").get("author").get("date").asText()));
				String authorName = jsonNode.get("commit").get("author").get("name").asText();
				String authorEmail = jsonNode.get("commit").get("author").get("email").asText();
				commit.setAuthor(new Contributor(authorName, authorEmail));
				commit.setCommitterDate(getDateFromString(jsonNode.get("commit").get("committer").get("date").asText()));
				String commiterName = jsonNode.get("commit").get("committer").get("name").asText();
				String commiterEmail = jsonNode.get("commit").get("committer").get("email").asText();
				commit.setCommitter(new Contributor(commiterName, commiterEmail));
				commit.setMessage(jsonNode.get("commit").get("message").asText());
				commits.add(commit);
			}
			return commits;
		}else {
			throw new Exception("Error searching file commits of repo: "+repoFullName+" of file: "+filePath+" ; content: "+content);
		}
	}
	
	public static Date getDateFromString(String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
	}

	public static List<Contributor> getContributorsFromCommits(List<CommitCitation> commits){
		List<Contributor> contributors = new ArrayList<Contributor>();
		forCommit: for (CommitCitation commit : commits) {
			Contributor contributor = commit.getAuthor();
			for (Contributor contributor2 : contributors) {
				if (contributor2.equals(contributor)) {
					continue forCommit;
				}
			}
			contributors.add(contributor);
		}
		return contributors;
	}

	private static ChatgptConversation getConversationOfOpenAiJson(String json) throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(json);
		JsonNode dataNode = rootNode.get("props").get("pageProps")
				.get("serverResponse").get("data");
		ChatgptConversation conversation = new ChatgptConversation();
		Long conversationCreateTime = dataNode.get("create_time").asLong();
		Long conversationUpdateTime = dataNode.get("update_time").asLong();
		conversation.setCreateTime(conversationCreateTime);
		conversation.setUpdateTime(conversationUpdateTime);
		if(dataNode != null) {
			JsonNode conversationsNode = dataNode.get("linear_conversation");
			for (JsonNode node : conversationsNode) {
				JsonNode messageNode = node.get("message");
				if(messageNode != null) {
					JsonNode authorNode = messageNode.get("author");
					if(authorNode != null && ChatgptUserAgent.getValuesArray().contains(authorNode.get("role").asText())) {
						ConversationTurn conversationTurn = new ConversationTurn();
						String agent = authorNode.get("role").asText();
						ChatgptUserAgent userAgent = ChatgptUserAgent.getByAgent(agent);
						conversationTurn.setUserAgent(userAgent);
						Long promptCreateTime = messageNode.get("create_time").asLong();
						conversationTurn.setCreateTime(promptCreateTime);
						JsonNode contentParts = messageNode.get("content").get("parts");
						for (JsonNode content : contentParts) {
							String fullContent = content.asText();
							List<PromptCode> codes = null;
							if(agent.equals(ChatgptUserAgent.ASSISTANT.getAgent()) && fullContent.contains(Constants.openAiCodeJsonDelimiter)) {
								codes = getCodesFromOpenAiJson(fullContent);
								for (PromptCode code : codes) {
									fullContent = fullContent.replace(code.getCodeFullText(), code.getCodeId());
								}
							}
							conversationTurn.setCodes(codes);
							conversationTurn.setFullText(fullContent);
						}
						conversation.getConversationTurns().add(conversationTurn);
					}
				}
			}
		}
		System.out.println();
		return conversation;
	}

	private static List<PromptCode> getCodesFromOpenAiJson(String fullContent) {
		List<PromptCode> promptCode = new ArrayList<PromptCode>();
		Pattern r = Pattern.compile(Constants.openAiCodeJsonDelimiter+".*?"+Constants.openAiCodeJsonDelimiter, 
				Pattern.DOTALL);
		Matcher m = r.matcher(fullContent);
		int codeId = 0;
		while (m.find()) {
			String codeText = m.group(0).trim();
			String codeBlock = codeText.replace(Constants.openAiCodeJsonDelimiter, "");
			String[] words = codeBlock.split("\\s+");
			String language = words.length > 0 ? words[0]:"";
			int firstNewlineIndex = codeBlock.indexOf("\n");
			int lastNewlineIndex = codeBlock.lastIndexOf("\n");
			if(firstNewlineIndex != -1 && lastNewlineIndex != -1) {
				codeBlock = codeBlock.substring(firstNewlineIndex+1, lastNewlineIndex);
				promptCode.add(new PromptCode(language, codeText, codeBlock, PromptCode.startOfCodeBlockId()+codeId));
				codeId++;
			}
		}
		return promptCode;
	}

	private static String getOpenAiJson(String url) {
		String cookie = System.getenv("OPENAI_COOKIE");
		try {
			String[] command = {"curl", "-b", cookie, url};

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
			int startIndex = html.indexOf(Constants.openAiJsonStart);
			int endIndex = html.indexOf(Constants.openAiJsonEnd);
			if(startIndex != -1 && endIndex != -1) {
				return html.substring(startIndex + Constants.openAiJsonStart.length(), endIndex);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();

		}
		return null;
	}

	public static File getFileContent(String token, String repoFullName, String filePath) throws Exception {
		filePath = UriUtils.encodePath(filePath, "UTF-8");
		ObjectMapper objectMapper = new ObjectMapper();
		String[] command = {"curl", "-L", "-H", "Accept: application/vnd.github+json", "-H", "Authorization: Bearer "+token, 
				"https://api.github.com/repos/"+repoFullName+"/contents/"+filePath}; 
		String content = GitHubCall.generalCall(command);
		JsonNode rootNode = objectMapper.readTree(content);
		if(rootNode.get("name") != null) {
			File file = new File();
			file.setName(rootNode.get("name").asText());
			file.setPath(rootNode.get("path").asText());
			file.setSha(rootNode.get("sha").asText());
			file.setSize(rootNode.get("size").asInt());
			file.setUrl(rootNode.get("url").asText());
			file.setHtmlUrl(rootNode.get("html_url").asText());
			file.setGitUrl(rootNode.get("git_url").asText());
			file.setDownloadUrl(rootNode.get("download_url").asText());
			file.setContentEncoded(rootNode.get("content").asText());
			file.setContentDecoded(decodeBase64(rootNode.get("content").asText()));
			return file;
		}else {
			throw new Exception("Error searching file: "+filePath+" of repository"+repoFullName+" ; error content"+content);
		}
	}

//	public static CommitContent getCommitContent(String token, String repoFullName, String commitSha) throws Exception {
//		ObjectMapper objectMapper = new ObjectMapper();
//		String[] command = {"curl", "-L", "-H", "Accept: application/vnd.github+json", "-H", "Authorization: Bearer "+token, 
//				"https://api.github.com/repos/"+repoFullName+"/commits/"+commitSha}; 
//		String content = GitHubCall.generalCall(command);
//		JsonNode rootNode = objectMapper.readTree(content.toString());
//		try {
//			if(rootNode.get("sha") != null) {
//				CommitContent commit = new CommitContent();
//				commit.setSha(rootNode.get("sha").asText());
//				commit.setNodeId(rootNode.get("node_id").asText());
//				commit.setUrl(rootNode.get("url").asText());
//				commit.setHtmlUrl(rootNode.get("html_url").asText());
//				commit.setCommentsUrl(rootNode.get("comments_url").asText());
//				commit.setCommitFiles(new ArrayList<>());
//				if (rootNode.get("files") != null && rootNode.get("files").size() > 0) {
//					for (JsonNode item : rootNode.get("files")) {
//						CommitFile commitFile = new CommitFile();
//						commitFile.setSha(item.get("sha").asText());
//						commitFile.setFileName(item.get("filename").asText());
//						commitFile.setAdditions(item.get("additions").asInt());
//						commitFile.setDeletions(item.get("deletions").asInt());
//						commitFile.setChanges(item.get("changes").asInt());
//						commitFile.setPatch(item.get("patch").asText());
//						commitFile.setStatus(item.get("status").asText());
//						commit.getCommitFiles().add(commitFile);
//					}
//				}
//				return commit;
//			}else {
//				throw new Exception("Error searching commit: "+commitSha+" of repository"+repoFullName+" ; error content"+content);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println();
//		}
//		return null;
//	}

	public static String decodeBase64(String encode) {
		String sanitizedEncodedString = encode.replaceAll("\\s", "");
		byte[] decodedBytes = Base64.getDecoder().decode(sanitizedEncodedString);
		return new String(decodedBytes);
	}

}