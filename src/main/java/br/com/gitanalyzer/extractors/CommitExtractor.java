package br.com.gitanalyzer.extractors;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.gitanalyzer.enums.OperationType;
import br.com.gitanalyzer.model.Commit;
import br.com.gitanalyzer.model.CommitFile;
import br.com.gitanalyzer.model.Contributor;
import br.com.gitanalyzer.model.File;
import br.com.gitanalyzer.model.Project;
import br.com.gitanalyzer.utils.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitExtractor {

	public List<Commit> getCommits(String projectPath, Project project) {
		List<Commit> commits = new ArrayList<Commit>();
		try {
			FileInputStream fstream = new FileInputStream(projectPath+Constants.commitFileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] commitSplited = strLine.split(";");
				String idCommit = commitSplited[0];
				String authorName = commitSplited[1];
				String authorEmail = commitSplited[2];
				String time = commitSplited[3];
				Contributor contributor = new Contributor(authorName, authorEmail);
				Integer timeInt = Integer.parseInt(time);
				Instant instant = Instant.ofEpochSecond(timeInt);
				Date commitDate = Date.from(instant);
				Commit commit = new Commit(contributor, project, commitDate, idCommit);
				commits.add(commit);
			}
			br.close();
			return commits;
		} catch (Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}


	public void extractCommitsFileAndDiffsOfCommits(String projectPath, List<Commit> commits, List<File> files) {
		for (Commit commit : commits) {
			List<CommitFile> commitsFiles = new ArrayList<CommitFile>();
			try {
				FileInputStream fstream = new FileInputStream(projectPath+Constants.commitFileFileName);
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
				String strLine;
				while ((strLine = br.readLine()) != null) {
					String[] splited = strLine.split(";");
					String id = splited[0];
					if (id.equals(commit.getExternalId())) {
						String operation = splited[1];
						String filePath = splited[3];
						File file = null;
						for (File fileCommitFile : files) {
							if (fileCommitFile.isFile(filePath)) {
								file = fileCommitFile;
								break;
							}
						}
						if(file == null) {
							continue;
						}
						CommitFile commitFile = new CommitFile(file, commit, 
								OperationType.getEnumByType(operation));
						commitsFiles.add(commitFile);
					}
				}
				List<FileLinesAdded> flas = numberOfAddedLines(projectPath, commit.getExternalId(), files);
				for (CommitFile cf : commitsFiles) {
					forFla:for (FileLinesAdded fla : flas) {
						if (cf.getFile().isFile(fla.path)) {
							cf.setAdds(fla.linesAdded);
							break forFla;
						}
					}
				}
				commit.setCommitFiles(commitsFiles);
				br.close();
			}catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	private List<FileLinesAdded> numberOfAddedLines(String projectPath, String idCommit, List<File> files) {
		List<FileLinesAdded> fla = new ArrayList<FileLinesAdded>();
		try {
			FileInputStream fstream = new FileInputStream(projectPath+Constants.diffFileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			boolean finded = false;
			while ((strLine = br.readLine()) != null) {
				if (finded == false) {
					String[] splited = strLine.split(" ");
					String string1 = splited[0];
					if (string1.equals("commit")) {
						String idCommitString = splited[1];
						if (idCommitString.equals(idCommit)) {
							finded = true;
							continue;
						}
					}
				}else {
					if (strLine.trim().isEmpty()) {
						break;
					}else {
						String[] splited = strLine.split(" ");
						String string1 = splited[0];
						if (string1.equals("commit")) {
							break;
						}
					}
					String[] splited = strLine.split("\t");
					String path = splited[2];
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
							String string1 = path.substring(path.indexOf("{") + 1);
							path = string1.substring(0, string1.indexOf("}"));
						}

						String[] splited1 = path.split("=>");
						String path1 = splited1[0];
						path1 = path1.trim();
						String path2 = splited1[1];
						path2 = path2.trim();

						String file1 = commonString1+path1+commonString2;
						String file2 = commonString1+path2+commonString2;
						for (File file : files) {
							if(file.isFile(file1) || file.isFile(file2)) {
								FileLinesAdded fileLinesAdded = new FileLinesAdded();
								fileLinesAdded.path = file.getPath();
								fileLinesAdded.linesAdded = Integer.parseInt(splited[0]);
								fla.add(fileLinesAdded);
							}
						}
					}else {
						for (File file : files) {
							if(file.isFile(path)) {
								FileLinesAdded fileLinesAdded = new FileLinesAdded();
								fileLinesAdded.path = file.getPath();
								fileLinesAdded.linesAdded = Integer.parseInt(splited[0]);
								fla.add(fileLinesAdded);
							}
						}
					}
				}
			}
			br.close();
			return fla;
		}catch (Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}

	class FileLinesAdded{
		int linesAdded;
		String path;
	}

}
