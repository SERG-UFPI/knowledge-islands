package br.com.gitanalyzer.analysis.dev_gpt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import br.com.gitanalyzer.model.Commit;
import br.com.gitanalyzer.model.CommitFile;
import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.SharedLink;
import br.com.gitanalyzer.model.github_openai.FileCitation;
import br.com.gitanalyzer.model.github_openai.FileLinkAuthor;
import br.com.gitanalyzer.service.SharedLinkService;
import br.com.gitanalyzer.utils.AsyncUtils;

public class FileCitationSearch {

	public static List<FileCitation> getFilesFromFilesCitations(String token) {
		try {
			//String json = DevGptSearches.getGithubGraphqlFileBlame("OtavioCury", "knowledge-islands", "main", "src/main/java/br/com/gitanalyzer/api/AuthController.java");
			List<SharedLink> sharedLinks = SharedLinkService.getFileSharedLinks(token);
			List<String> repos = new ArrayList<>();
			for (SharedLink sharedLink : sharedLinks) {
				if(!repos.contains(sharedLink.getRepository().getFullName())) {
					repos.add(sharedLink.getRepository().getFullName());
				}
			}
			List<File> filesSharedLinks = DevGptSearches.getFilesFromSharedLinks(sharedLinks);
			ExecutorService executorService = AsyncUtils.getExecutorServiceForLogs();
			List<CompletableFuture<Void>> futures = new ArrayList<>();
			for(File file: filesSharedLinks) {
				CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
					try {
						File fileContent = DevGptSearches.getFileContent(token, 
								file.getRepository().getFullName(), file.getPath());
						file.setContentDecoded(fileContent.getContentDecoded());
						file.setContentEncoded(fileContent.getContentEncoded());
						file.setDownloadUrl(fileContent.getDownloadUrl());
						file.setSize(fileContent.getSize());
						file.setEncoding(fileContent.getEncoding());
						file.setCommits(DevGptSearches.getCommitsOfFile(token, file.getRepository().getFullName(), file.getPath()));
						for (Commit commit : file.getCommits()) {
							commit.setCommitFiles(DevGptSearches.getCommitFiles(token, file.getRepository().getFullName(), commit.getSha()));
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
				}, executorService);
				futures.add(future);
			}
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
			executorService.shutdown();
			filesSharedLinks = filesSharedLinks.stream().filter(f -> f.getSize() != 0).toList();
			filesSharedLinks = filesSharedLinks.stream().filter(f -> f.getCommits() != null && f.getCommits().size() > 0).toList();
			for (SharedLink sharedLink : sharedLinks) {
				for (FileLinkAuthor fileLinkAuthor : sharedLink.getFilesLinkAuthor()) {
					File file = filesSharedLinks.stream().filter(f -> 
					f.getRepository().getFullName().equals(fileLinkAuthor.getAuthorFile().getFile().getRepository().getFullName()) 
					&& f.getPath().equals(fileLinkAuthor.getAuthorFile().getFile().getPath())).findFirst().get();
					fileLinkAuthor.getAuthorFile().setFile(file);
				}
			}
			sharedLinks = sharedLinks.stream().filter(s -> s.getFilesLinkAuthor() != null && s.getFilesLinkAuthor().size() > 0).toList();
			for (SharedLink sharedLink: sharedLinks) {
				for (FileLinkAuthor fileLinkAuthor : sharedLink.getFilesLinkAuthor()) {
					try {
						Commit commitThatAddedLink = DevGptSearches.getCommitThatAddedLink(fileLinkAuthor.getAuthorFile().getFile(), sharedLink.getLink());
						sharedLink.setCommitThatAddedTheLink(commitThatAddedLink);
						CommitFile commitFile = commitThatAddedLink.getCommitFiles().stream()
								.filter(cf -> cf.getFile().getPath().equals(fileLinkAuthor.getAuthorFile().getFile().getPath())).findFirst().get();
						List<String> codes = DevGptSearches.getCodesFromConversation(sharedLink.getConversation().getConversationTurns());
						fileLinkAuthor.setLinesCopied(DevGptSearches.getLinesCopied(codes, commitFile.getAddedLines()));
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			sharedLinks = sharedLinks.stream().filter(s -> s.getCommitThatAddedTheLink() != null).toList();
			for (SharedLink sharedLink : sharedLinks) {
				for (FileLinkAuthor fileLinkAuthor : sharedLink.getFilesLinkAuthor()) {
					List<CommitFile> commitFiles = DevGptSearches.getAllCommitFilesOfAuthor(fileLinkAuthor.getAuthorFile().getFile(), 
							sharedLink.getCommitThatAddedTheLink().getAuthor());
				}
			}
			//			ExecutorService executorService3 = AsyncUtils.getExecutorServiceForLogs();
			//			List<CompletableFuture<Void>> futures3 = new ArrayList<>();
			//			for (FileCitation fileCitation : filesCitations) {
			//				CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
			//					try {
			//						for (Commit commit : fileCitation.getFileAuthor().getFile().getCommits()) {
			//							commit.setCommitFiles(DevGptSearches.getCommitFiles(token, fileCitation.getRepositoryFullName(), commit.getSha()));
			//						}
			//						for (SharedLink link: fileCitation.getSharedLinks()) {
			//							Commit commitThatAddedLink = DevGptSearches.getCommitThatAddedLink(fileCitation.getFileAuthor().getFile(), link.getLink());
			//							link.setCommitThatAddedTheLink(commitThatAddedLink);
			//							CommitFile commitFile = link.getCommitThatAddedTheLink().getCommitFiles().stream()
			//									.filter(cf -> cf.getFilePath().equals(fileCitation.getFileAuthor().getFile().getPath())).findFirst().get();
			//							List<String> codes = DevGptSearches.getCodesFromConversation(link.getConversation().getConversationTurns());
			//							link.setLinesCopied(DevGptSearches.getLinesCopied(codes, commitFile.getAddedLines()));
			//						}
			//					}catch (Exception e) {
			//						e.printStackTrace();
			//					}
			//				}, executorService3);
			//				futures3.add(future);
			//			}
			//			CompletableFuture.allOf(futures3.toArray(new CompletableFuture[0])).join();
			//			executorService3.shutdown();
			//			for (FileCitation fileCitation : filesCitations) {
			//				fileCitation.setSharedLinks(fileCitation.getSharedLinks().stream().filter(sl -> sl.getCommitThatAddedTheLink() != null).toList());
			//			}
			//			filesCitations = filesCitations.stream().filter(fc -> fc.getSharedLinks() != null && fc.getSharedLinks().size() > 0).toList();
			//			for (FileCitation fileCitation : filesCitations) {
			//				for (SharedLink sharedLink : fileCitation.getSharedLinks()) {
			//					List<Commit> commtis = DevGptSearches.getAllCommitsOfAuthor(fileCitation.getFileAuthor().getFile().getCommits(), sharedLink.getCommitThatAddedTheLink().getAuthor());
			//					System.out.println();
			//				}
			//			}
			//			return filesCitations;
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
		return null;
	}

}
