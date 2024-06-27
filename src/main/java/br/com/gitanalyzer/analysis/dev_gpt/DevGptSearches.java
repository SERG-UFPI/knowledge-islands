package br.com.gitanalyzer.analysis.dev_gpt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

import br.com.gitanalyzer.enums.OperationType;
import br.com.gitanalyzer.exceptions.FetchPageException;
import br.com.gitanalyzer.exceptions.FileNotFoundOnCommitException;
import br.com.gitanalyzer.exceptions.LinkNotFoundOnCommitsException;
import br.com.gitanalyzer.exceptions.SharedLinkNotFoundException;
import br.com.gitanalyzer.extractors.GitRepositoryTruckFactorExtractor;
import br.com.gitanalyzer.model.Commit;
import br.com.gitanalyzer.model.CommitFile;
import br.com.gitanalyzer.model.entity.ChatgptConversation;
import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.ConversationTurn;
import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.PromptCode;
import br.com.gitanalyzer.model.github_openai.enums.ChatgptUserAgent;
import br.com.gitanalyzer.utils.Constants;
import br.com.gitanalyzer.utils.FileUtils;


public class DevGptSearches {

	public static void main(String[] args) throws Exception {
		String token = args[0];
		//List<FileCitation> fileCitations = FileCitationSearch.getFilesFromFilesCitations(token);
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

	public static String getGithubGraphqlFileBlame(String user, String repository, String main, String filePath) throws IOException, URISyntaxException, InterruptedException {
		String pathBlameScript = DevGptSearches.class.getResource("/scripts_shell/github_graphql_blame.sh").toURI().getPath();
		String command = "sh "+pathBlameScript+" "+user+" "+repository+" "+main+" "+filePath;
		ProcessBuilder pb = new ProcessBuilder(new String[] {"bash", "-l", "-c", command});
		Process process = pb.start();
		StringBuilder output = new StringBuilder();
		new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
		process.waitFor();
		return output.toString();
	}

	public static List<String> getLinesCopied(List<String> code, List<String> addedLines) {
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

	//	public static List<Contributor> getContributorsFromCommits(List<CommitCitation> commits){
	//		List<Contributor> contributors = new ArrayList<Contributor>();
	//		forCommit: for (CommitCitation commit : commits) {
	//			Contributor contributor = commit.getAuthor();
	//			for (Contributor contributor2 : contributors) {
	//				if (contributor2.equals(contributor)) {
	//					continue forCommit;
	//				}
	//			}
	//			contributors.add(contributor);
	//		}
	//		return contributors;
	//	}

	public static ChatgptConversation getConversationOfOpenAiJson(String json) throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(json);
		JsonNode dataNode = rootNode.get("props").get("pageProps")
				.get("serverResponse").get("data");
		ChatgptConversation conversation = new ChatgptConversation();
		Date createTime = new java.util.Date(dataNode.get("create_time").asLong()*1000);
		conversation.setCreateTime(createTime);
		Date updateTime = new java.util.Date(dataNode.get("update_time").asLong()*1000);
		conversation.setUpdateTime(updateTime);
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
						if(contentParts != null) {
							for (JsonNode content : contentParts) {
								String fullContent = content.asText();
								List<PromptCode> codes = null;
								if(agent.equals(ChatgptUserAgent.ASSISTANT.getAgent()) && fullContent.contains(Constants.openAiCodeJsonDelimiter)) {
									codes = getCodesFromOpenAiJson(fullContent);
									for (PromptCode code : codes) {
										fullContent = fullContent.replace(code.getCodeFullText(), code.getCodeId());
									}
									conversationTurn.setCodes(codes);
								}
								conversationTurn.setFullText(fullContent);
							}
							conversation.addConversationTurn(conversationTurn);
						}
					}
				}
			}
		}
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

	public static String getOpenAiJson(String url) throws SharedLinkNotFoundException, FetchPageException {
		int errorNotFound = 404; 
		ObjectMapper objectMapper = new ObjectMapper();
		String cookie = System.getenv("OPENAI_COOKIE");
		try {
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
			int startIndex = html.indexOf(Constants.openAiJsonStart);
			int endIndex = html.indexOf(Constants.openAiJsonEnd);
			if(startIndex != -1 && endIndex != -1) {
				String json = html.substring(startIndex + Constants.openAiJsonStart.length(), endIndex);
				JsonNode rootNode = objectMapper.readTree(json);
				if(rootNode.get("props") != null && rootNode.get("props").get("pageProps") != null 
						&& rootNode.get("props").get("pageProps").get("statusCode") != null) {
					int status = rootNode.get("props").get("pageProps").get("statusCode").asInt();
					if(status == errorNotFound) {
						throw new SharedLinkNotFoundException(url);
					}
				}
				return json;
			}else {
				throw new FetchPageException(url);
			}
		} catch (Exception e) {
			throw new FetchPageException(url);
		}
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
			file.setEncoding(rootNode.get("encoding").asText());
			return file;
		}else {
			throw new Exception("Error searching file: "+filePath+" of repository"+repoFullName+" ; error content"+content);
		}
	}

	public static List<CommitFile> getCommitFiles(String token, String repoFullName, String commitSha) throws Exception {
		try {
			List<CommitFile> commitFiles = new ArrayList<>();
			ObjectMapper objectMapper = new ObjectMapper();
			String[] command = {"curl", "-L", "-H", "Accept: application/vnd.github+json", "-H", "Authorization: Bearer "+token, 
					"https://api.github.com/repos/"+repoFullName+"/commits/"+commitSha}; 
			String content = GitHubCall.generalCall(command);
			JsonNode rootNode = objectMapper.readTree(content.toString());
			if (rootNode.get("files") != null && rootNode.get("files").size() > 0) {
				for (JsonNode item : rootNode.get("files")) {
					CommitFile commitFile = new CommitFile();
					commitFile.getFile().setSha(item.get("sha").asText());
					commitFile.getFile().setPath(item.get("filename").asText());
					commitFile.setAdditions(item.get("additions").asInt());
					commitFile.setDeletions(item.get("deletions").asInt());
					commitFile.setChanges(item.get("changes").asInt());
					commitFile.setPatch(item.get("patch")!=null?item.get("patch").asText():null);
					commitFile.setAddedLines(commitFile.getPatch() != null ? getAddedLinesFromPatch(commitFile.getPatch()):null);
					commitFile.setStatus(OperationType.valueOf(item.get("status").asText().toUpperCase()));
					commitFiles.add(commitFile);
				}
			}
			return commitFiles;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<String> getCodesFromConversation(List<ConversationTurn> conversation){
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

	public static String decodeBase64(String encode) {
		String sanitizedEncodedString = encode.replaceAll("\\s", "");
		byte[] decodedBytes = Base64.getDecoder().decode(sanitizedEncodedString);
		return new String(decodedBytes);
	}

	public static List<String> getAddedLinesFromPatch(String patch){
		List<String> addedLines = new ArrayList<>();
		String[] lines = patch.split("\n");
		for (String line : lines) {
			if(line.startsWith("+")) {
				addedLines.add(line.substring(1));
			}
		}
		return addedLines;
	}

	public static Commit getCommitThatAddedLink(File file, String link) throws LinkNotFoundOnCommitsException, FileNotFoundOnCommitException {
		for (Commit commit : file.getCommits()) {
			for (CommitFile commitFile : commit.getCommitFiles()) {
				if(commitFile.getFile().getPath().equals(file.getPath())) {
					if(commitFile.getAddedLines() != null && commitFile.getAddedLines().size() > 0) {
						if(commitFile.getAddedLines().stream().anyMatch(a -> a.contains(link))) {
							return commit;
						}
					}
				}
			}
		}
		throw new LinkNotFoundOnCommitsException(link, file.getPath());
	}

	public static List<Commit> getAllCommitsOfAuthor(List<Commit> commits, Contributor contributor){
		List<Commit> commitsAuthor = new ArrayList<>();
		for (Commit commit : commits) {
			if(commit.getAuthor().getEmail().equals(contributor.getEmail()) 
					|| GitRepositoryTruckFactorExtractor.checkAliasContributors(contributor, commit.getAuthor())) {
				commitsAuthor.add(commit);
			}
		}
		return commitsAuthor;
	}
	
	public static List<CommitFile> getAllCommitFilesOfAuthor(File file, Contributor contributor){
		List<CommitFile> commitFilesAuthor = new ArrayList<>();
		for (Commit commit : file.getCommits()) {
			if(commit.getAuthor().getEmail().equals(contributor.getEmail()) 
					|| GitRepositoryTruckFactorExtractor.checkAliasContributors(contributor, commit.getAuthor())) {
				commitFilesAuthor.add(commit.getCommitFiles().stream().filter(cf -> cf.getFile().getPath().equals(file.getPath())).findFirst().get());
				continue;
			}
		}
		return commitFilesAuthor;
	}

//	public static List<File> getFilesFromSharedLinks(List<SharedLink> sharedLinks) {
//		List<File> files = new ArrayList<>();
//		for (SharedLink sharedLink : sharedLinks) {
//			for (FileLinkAuthor fileLinkAuthor: sharedLink.getFilesLinkAuthor()) {
//				if(!files.stream().anyMatch(f -> f.getPath().equals(fileLinkAuthor.getAuthorFile().getFileVersion().getFile().getPath()) 
//						&& f.getRepository().getFullName().equals(fileLinkAuthor.getAuthorFile().getFileVersion().getFile().getRepository().getFullName()))) {
//					files.add(fileLinkAuthor.getAuthorFile().getFileVersion().getFile());
//				}
//			}
//		}
//		return files;
//	}
}