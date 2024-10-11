package br.com.gitanalyzer.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.model.entity.Commit;
import br.com.gitanalyzer.model.entity.CommitFile;
import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.FileGitRepositorySharedLinkCommit;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.enums.OperationType;
import br.com.gitanalyzer.repository.FileGitRepositorySharedLinkCommitRepository;
import br.com.gitanalyzer.utils.Constants;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class CommitService {

	@Autowired
	private FileGitRepositorySharedLinkCommitRepository fileGitRepositorySharedLinkCommitRepository;
	@Autowired
	private ContributorService contributorService;

	public List<Commit> getCommitsFiles(GitRepository gitRepository, List<Commit> commits, List<File> files, 
			List<FileGitRepositorySharedLinkCommit> fileSharedLink) throws IOException {
		FileInputStream fstream = new FileInputStream(gitRepository.getCurrentFolderPath()+Constants.commitFileFileName);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(fstream));){
			String strLine;
			whileFile: while ((strLine = br.readLine()) != null) {
				String[] splited = strLine.split(";");
				String id = splited[0];
				for (Commit commit : commits) {
					if (id.equals(commit.getSha())) {
						String operation = splited[1];
						String filePath = splited[3];
						for (File file : files) {
							if (file.isFile(filePath)) {
								commit.getCommitFiles().add(new CommitFile(file, OperationType.valueOf(operation)));
								continue whileFile;
							}
						}
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		return commits;
	}

	public List<Commit> getCommitsFromLogFiles(String projectPath) throws IOException {
		List<Commit> commits = new ArrayList<>();
		List<Contributor> contributors = new ArrayList<>();
		FileInputStream fstream = new FileInputStream(projectPath+Constants.commitFileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		try {
			String strLine;
			while ((strLine = br.readLine()) != null) {
				try {
					String[] commitSplited = strLine.split(";");
					if(commitSplited.length >= 4) {
						String idCommit = commitSplited[0];
						String authorName = commitSplited[1];
						String authorEmail = commitSplited[2];
						if(authorName != null && authorEmail != null) {
							String time = commitSplited[3];
							String message = null;
							if(commitSplited.length == 5) {
								message = commitSplited[4];
							}
							Contributor contributorCommit = null;
							for (Contributor contributor : contributors) {
								if(contributor.getName().equals(authorName)
										&& contributor.getEmail().equals(authorEmail)) {
									contributorCommit = contributor;
									break;
								}
							}
							if(contributorCommit == null) {
								contributorCommit = new Contributor(authorName, authorEmail);
								contributors.add(contributorCommit);
							}
							Integer timeInt = Integer.parseInt(time);
							Instant instant = Instant.ofEpochSecond(timeInt);
							Date commitDate = Date.from(instant);
							commits.add(new Commit(contributorCommit, commitDate, idCommit, message));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		} finally {
			br.close();
		}
		return commits;
	}

	public List<Commit> getCommitsFileAndDiffsOfCommits(String projectPath, List<Commit> commits) throws IOException {
		FileInputStream fstream = new FileInputStream(projectPath+Constants.diffFileName);
		try(BufferedReader br = new BufferedReader(new InputStreamReader(fstream));) {
			String strLine;
			Commit commitAnalyzed = null;
			whileFile:while ((strLine = br.readLine()) != null) {
				if (strLine.trim().isEmpty()) {
					continue whileFile;
				}
				String[] splited1 = strLine.split(" ");
				String string1 = splited1[0];
				if (string1.equals("commit")) {
					String idCommitString = splited1[1];
					for (Commit commit : commits) {
						if (idCommitString.equals(commit.getSha())) {
							commitAnalyzed = commit;
							continue whileFile;
						}
					}
					commitAnalyzed = null;
					continue whileFile;
				}
				if (commitAnalyzed != null) {
					try {
						String[] splited2 = strLine.split("\t");
						String path = splited2[2];
						if (path.contains("=>")) {
							String commonString1 = "";
							String commonString2 = "";
							if (path.contains("{") && path.contains("}")) {
								String[] commonString =  path.split("\\{");
								commonString1 = commonString[0];
								commonString = path.split("}");
								if (commonString.length > 1) {
									commonString2 = commonString[1];
								}
								String stringAux = path.substring(path.indexOf("{") + 1);
								path = stringAux.substring(0, stringAux.indexOf("}"));
							}

							String[] splited3 = path.split("=>");
							String path1 = splited3[0];
							path1 = path1.trim();
							String file1 = commonString1+path1+commonString2;
							if(file1.contains("//")) {
								file1 = file1.replace("//", "/");
							}
							String path2 = splited3[1];
							path2 = path2.trim();
							String file2 = commonString1+path2+commonString2;
							if(file2.contains("//")) {
								file2 = file2.replace("//", "/");
							}
							for (CommitFile commitFile : commitAnalyzed.getCommitFiles()) {
								if(commitFile.getFile().isFile(file1) || commitFile.getFile().isFile(file2)) {
									try {
										int linesAdded = Integer.parseInt(splited2[0]);
										commitFile.setAdditions(linesAdded);
									} catch (Exception e) {
										e.printStackTrace();
									}
									continue whileFile;
								}
							}
						}else {
							for (CommitFile commitFile : commitAnalyzed.getCommitFiles()) {
								if(commitFile.getFile().isFile(path)) {
									try {
										int linesAdded = Integer.parseInt(splited2[0]);
										commitFile.setAdditions(linesAdded);
									} catch (Exception e) {
										e.printStackTrace();
									}
									continue whileFile;
								}
							}
						}	
					} catch (ArrayIndexOutOfBoundsException e) {
						e.printStackTrace();
						log.info("Error processing project diff "+e.getMessage());
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return commits;
	}

	public List<Commit> getAllCommitsOfAuthor(List<Commit> commits, Contributor contributor){
		List<Commit> commitsAuthor = new ArrayList<>();
		for (Commit commit : commits) {
			if(commit.getAuthor().getEmail().equals(contributor.getEmail()) 
					|| contributorService.checkAliasContributors(contributor, commit.getAuthor())) {
				commitsAuthor.add(commit);
			}
		}
		return commitsAuthor;
	}
}
