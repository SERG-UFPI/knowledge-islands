package br.com.knowledgeislands.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.knowledgeislands.model.entity.Commit;
import br.com.knowledgeislands.model.entity.CommitFile;
import br.com.knowledgeislands.model.entity.Contributor;
import br.com.knowledgeislands.model.entity.File;
import br.com.knowledgeislands.model.entity.GitRepository;
import br.com.knowledgeislands.model.entity.GitRepositoryVersion;
import br.com.knowledgeislands.model.enums.OperationType;
import br.com.knowledgeislands.repository.GitRepositoryVersionRepository;
import br.com.knowledgeislands.utils.KnowledgeIslandsUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class CommitService {

	@Autowired
	private ContributorService contributorService;
	@Autowired
	private GitRepositoryVersionRepository gitRepositoryVersionRepository;

	public List<Commit> getCommitsFiles(GitRepository gitRepository, List<Commit> commits, List<File> files) throws IOException {
		Map<String, File> fileMap = new HashMap<>();
		for (File file : files) {
			fileMap.put(file.getPath(), file);
			for (String renamePath : file.getRenamePaths()) {
				fileMap.put(renamePath, file);
			}
		}
		Map<String, Commit> commitMap = commits.stream()
				.collect(Collectors.toMap(Commit::getSha, commit -> commit));
		FileInputStream fstream = new FileInputStream(gitRepository.getCurrentFolderPath()+KnowledgeIslandsUtils.commitFileFileName);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(fstream));){
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] splited = strLine.split(";");
				String id = splited[0].trim();
				Commit commit = commitMap.get(id);
				if (commit != null) {
					String operation = splited[1].trim();
					String filePath = KnowledgeIslandsUtils.removeEnclosingQuotes(splited[3].trim());
					File file = fileMap.get(filePath);
					if (file != null) {
						commit.getCommitFiles().add(new CommitFile(file, OperationType.valueOf(operation), commit));
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		commits.removeIf(c -> c.getCommitFiles().isEmpty());
		return commits;
	}

	private List<Contributor> addContributorsRepositoryVersions(Long gitRepositoryId) {
		List<Contributor> contributors = new ArrayList<>();
		List<GitRepositoryVersion> versions = gitRepositoryVersionRepository.findByGitRepositoryId(gitRepositoryId);
		if(versions != null) {
			versions.forEach(v -> contributors.addAll(v.getContributors()));
		}
		return contributors;
	}

	private String cleanEmail(String email) {
		email = email.trim();
		if(email.contains(" ")) {
			List<String> emails = new ArrayList<>();
			Pattern pattern = Pattern.compile(KnowledgeIslandsUtils.emailRegex);
			Matcher matcher = pattern.matcher(email);
			while (matcher.find()) {
				emails.add(matcher.group());
			}
			if(!emails.isEmpty()) {
				String smallest = emails.get(0);
				for (String str : emails) {
					if (str.length() < smallest.length()) {
						smallest = str;
					}
				}
				return smallest;
			}
		}
		return email;
	}

	public List<Commit> getCommitsFromLogFiles(GitRepository gitRepository) throws IOException {
		List<Commit> commits = new ArrayList<>();
		List<Contributor> contributors = addContributorsRepositoryVersions(gitRepository.getId());
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(gitRepository.getCurrentFolderPath()+KnowledgeIslandsUtils.commitFileName)));) {
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] commitSplited = strLine.split(";");
				if(commitSplited.length >= 4) {
					String idCommit = commitSplited[0].trim();
					String authorName = commitSplited[1].trim();
					String authorEmail = cleanEmail(commitSplited[2]);
					if(authorName != null && !authorName.isEmpty() && authorEmail != null && !authorEmail.isEmpty()) {
						String time = commitSplited[3].trim();
						String message = null;
						if(commitSplited.length == 5) {
							message = commitSplited[4].trim();
							message = message.length() > 1000 ? message.substring(0,1000): message;
						}
						Contributor contributorCommit = null;
						forContributor:for (Contributor contributor : contributors) {
							List<Contributor> contributorAliases = contributor.contributorAlias();
							for (Contributor alias : contributorAliases) {
								if(alias.getName().equals(authorName)
										&& alias.getEmail().equals(authorEmail)) {
									contributorCommit = contributor;
									break forContributor;
								}
							}
						}
						if(contributorCommit == null) {
							contributorCommit = new Contributor(authorName, authorEmail);
							contributors.add(contributorCommit);
						}
						try {
							Date commitDate = Date.from(Instant.ofEpochSecond(Long.parseLong(time)));
							commits.add(new Commit(contributorCommit, commitDate, idCommit, message));
						} catch (java.lang.NumberFormatException e) {
							e.printStackTrace();
							log.error(e.getMessage());
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return new ArrayList<>();
		}
		return commits;
	}

	public List<Commit> getCommitsFileAndDiffsOfCommits(String projectPath, List<Commit> commits) throws IOException {
		Map<String, Commit> commitMap = commits.stream()
				.collect(Collectors.toMap(Commit::getSha, commit -> commit));
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(projectPath+KnowledgeIslandsUtils.diffFileName)));) {
			String strLine;
			Commit commitAnalyzed = null;
			while ((strLine = br.readLine()) != null) {
				if (strLine.trim().isEmpty()) continue;
				String[] splited1 = strLine.split(" ");
				if (splited1.length > 1 && splited1[0].trim().equals("commit")) {
					String idCommitString = splited1[1].trim();
					commitAnalyzed = commitMap.get(idCommitString);
					continue;
				}
				if (commitAnalyzed != null) {
					String[] splited2 = strLine.split("\t");
					if(splited2.length < 3) continue;
					String path = splited2[2].trim();
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
						String path1 = splited3[0].trim();
						path1 = KnowledgeIslandsUtils.removeEnclosingQuotes(path1);
						String file1 = commonString1+path1+commonString2;
						file1 = file1.replace("//", "/");

						String path2 = splited3[1].trim();
						path2 = KnowledgeIslandsUtils.removeEnclosingQuotes(path2);
						String file2 = commonString1+path2+commonString2;
						file2 = file2.replace("//", "/");

						for (CommitFile commitFile : commitAnalyzed.getCommitFiles()) {
							if(commitFile.getFile().isFile(file1) || commitFile.getFile().isFile(file2)) {
								setLinesAdded(commitFile, splited2[0]);
								break;
							}
						}
					}else {
						path = KnowledgeIslandsUtils.removeEnclosingQuotes(path);
						for (CommitFile commitFile : commitAnalyzed.getCommitFiles()) {
							if(commitFile.getFile().isFile(path)) {
								setLinesAdded(commitFile, splited2[0]);
								break;
							}
						}
					}	
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return commits;
	}

	private void setLinesAdded(CommitFile commitFile, String numLines) {
		try {
			int linesAdded = Integer.parseInt(numLines.trim());
			commitFile.setAdditions(linesAdded);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
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

	public List<String> getCodeLinesAddedCommitFile(Repository repository, Commit commit, File file) {
		Set<String> filePaths = file.getFilePaths();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ObjectId commitId = ObjectId.fromString(commit.getSha());
		try(RevWalk revWalk = new RevWalk(repository)) {
			RevCommit rev = revWalk.parseCommit(commitId);
			List<DiffEntry> diffs = diffsForTheCommit(repository, rev);
			for (DiffEntry diff : diffs) {
				String newPath = diff.getNewPath();
				if(KnowledgeIslandsUtils.containsNonAscii(newPath)) {
					newPath = KnowledgeIslandsUtils.encodeNonAsciiOnly(newPath);
				}
				String oldPath = diff.getOldPath();
				if(KnowledgeIslandsUtils.containsNonAscii(newPath)) {
					oldPath = KnowledgeIslandsUtils.encodeNonAsciiOnly(oldPath);
				}
				if(filePaths.contains(newPath) || filePaths.contains(oldPath)) {
					DiffFormatter diffFormatter = new DiffFormatter( stream );
					diffFormatter.setRepository(repository);
					diffFormatter.format(diff);
					String in = stream.toString();
					diffFormatter.close();
					return getAddedLines(in);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		return new ArrayList<>();
	}

	public List<DiffEntry> diffsForTheCommit(Repository repo, RevCommit commit) throws IOException { 
		AnyObjectId currentCommit = repo.resolve(commit.getName()); 
		AnyObjectId parentCommit = commit.getParentCount() > 0 ? repo.resolve(commit.getParent(0).getName()) : null; 
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE); 
		//df.setBinaryFileThreshold(2 * 1024); //2 MB MAX A FILE
		df.setRepository(repo); 
		df.setDiffComparator(RawTextComparator.DEFAULT); 
		df.setDetectRenames(true); 
		List<DiffEntry> diffs = null; 
		if (parentCommit == null) { 
			RevWalk rw = new RevWalk(repo); 
			diffs = df.scan(new EmptyTreeIterator(), new CanonicalTreeParser(null, rw.getObjectReader(), commit.getTree())); 
			rw.close(); 
		} else { 
			diffs = df.scan(parentCommit, currentCommit); 
		} 
		df.close();
		return diffs; 
	}

	public List<String> getAddedLines(String fileDiff) {
		List<String> addedLines = new ArrayList<>();
		String[] lines = fileDiff.split("\\r?\\n");
		for (String line : lines) {
			if (line.startsWith("+") && !line.startsWith("+++")) {
				line = line.substring(1).replace("\t", "");
				if(!line.isEmpty() && !line.isBlank()) {
					addedLines.add(line);
				}
			}
		}
		return addedLines;
	}
}
