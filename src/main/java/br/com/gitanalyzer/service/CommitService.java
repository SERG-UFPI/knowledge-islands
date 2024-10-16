package br.com.gitanalyzer.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import br.com.gitanalyzer.model.entity.Commit;
import br.com.gitanalyzer.model.entity.CommitFile;
import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.enums.OperationType;
import br.com.gitanalyzer.utils.KnowledgeIslandsUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class CommitService {

	@Autowired
	private ContributorService contributorService;

	public List<Commit> getCommitsFiles(GitRepository gitRepository, List<Commit> commits, List<File> files) throws IOException {
		FileInputStream fstream = new FileInputStream(gitRepository.getCurrentFolderPath()+KnowledgeIslandsUtils.commitFileFileName);
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
		FileInputStream fstream = new FileInputStream(projectPath+KnowledgeIslandsUtils.commitFileName);
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
		FileInputStream fstream = new FileInputStream(projectPath+KnowledgeIslandsUtils.diffFileName);
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

	public List<String> getLinesAddedCommitFile(Repository repository, Commit commit, File file) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ObjectId commitId = ObjectId.fromString(commit.getSha());
		try(RevWalk revWalk = new RevWalk(repository)) {
			RevCommit rev = revWalk.parseCommit(commitId);
			List<DiffEntry> diffs = diffsForTheCommit(repository, rev);
			for (DiffEntry diff : diffs) {
				if(file.isFile(diff.getNewPath()) || file.isFile(diff.getOldPath())) {
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
		return null;
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
