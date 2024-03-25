package br.com.gitanalyzer.extractors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import br.com.gitanalyzer.enums.OperationType;
import br.com.gitanalyzer.model.Commit;
import br.com.gitanalyzer.model.CommitFile;
import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.utils.Constants;

public class CommitExtractor {
	
	private List<Date> getDatesOfProject(String projectPath) throws IOException{
		List<Date> dates = new ArrayList<Date>();
		FileInputStream fstream = new FileInputStream(projectPath+Constants.commitFileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		while ((strLine = br.readLine()) != null) {
			try {
				String[] commitSplited = strLine.split(";");
				if(commitSplited.length == 4) {
					String time = commitSplited[3];
					Integer timeInt = Integer.parseInt(time);
					Instant instant = Instant.ofEpochSecond(timeInt);
					Date commitDate = Date.from(instant);
					dates.add(commitDate);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		br.close();
		return dates;
	}
	
	public Date getLastCommitDate(String projectPath) throws IOException {
		List<Date> dates = getDatesOfProject(projectPath);
		if(dates != null && dates.size() > 0) {
			Collections.sort(dates);
			return dates.get(dates.size()-1);
		}else {
			return null;
		}
	}

	public Date getFirstCommitDate(String projectPath) throws IOException {
		List<Date> dates = getDatesOfProject(projectPath);
		if(dates != null && dates.size() > 0) {
			Collections.sort(dates);
			return dates.get(0);
		}else {
			return null;
		}
	}

	public String getLastCommitHash(String projectPath) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(projectPath+Constants.commitFileName));
		String hash = br.readLine().split(";")[0];
		br.close();
		return hash;
	}

	public List<Commit> getCommitsDatesAndHashes(String projectPath){
		List<Commit> commits = new ArrayList<Commit>();
		try {
			FileInputStream fstream = new FileInputStream(projectPath+Constants.commitFileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				try {
					String[] commitSplited = strLine.split(";");
					String idCommit = commitSplited[0];
					String time = commitSplited[3];
					Integer timeInt = Integer.parseInt(time);
					Instant instant = Instant.ofEpochSecond(timeInt);
					Date commitDate = Date.from(instant);
					Commit commit = new Commit(commitDate, idCommit);
					commits.add(commit);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			br.close();
			return commits;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<Commit> extractCommitsFromLogFiles(String projectPath) {
		List<Commit> commits = new ArrayList<Commit>();
		List<Contributor> contributors = new ArrayList<Contributor>();
		try {
			FileInputStream fstream = new FileInputStream(projectPath+Constants.commitFileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				try {
					String[] commitSplited = strLine.split(";");
					if(commitSplited.length == 4) {
						String idCommit = commitSplited[0];
						String authorName = commitSplited[1];
						String authorEmail = commitSplited[2];
						if(authorName != null && authorEmail != null 
								&& !authorEmail.contains(Constants.noreply)) {
							String time = commitSplited[3];
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
							commits.add(new Commit(contributorCommit, commitDate, idCommit));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			br.close();
			return commits;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<Commit> extractCommitsFiles(String projectPath, List<Commit> commits, List<File> files) {
		try {
			FileInputStream fstream = new FileInputStream(projectPath+Constants.commitFileFileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			whileFile: while ((strLine = br.readLine()) != null) {
				String[] splited = strLine.split(";");
				String id = splited[0];
				for (Commit commit : commits) {
					if (id.equals(commit.getExternalId())) {
						String operation = splited[1];
						String filePath = splited[3];
						for (File fileCommitFile : files) {
							if (fileCommitFile.isFile(filePath)) {
								commit.getCommitFiles().add(new CommitFile(fileCommitFile, OperationType.getEnumByType(operation)));
								continue whileFile;
							}
						}
					}
				}
			}
			br.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return commits;
	}

	public void generateCommitFileFile(String projectPath) {
		try {
			FileInputStream fstream = new FileInputStream(projectPath+Constants.logFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			String commitHash = null;
			List<String> lines = new ArrayList<String>();
			whileFile:while ((strLine = br.readLine()) != null) {
				if (strLine.trim().isEmpty()) {
					continue whileFile;
				}
				String[] splited = strLine.split("\t");
				String string1 = splited[0];
				if (string1.equals("commit")) {
					commitHash = splited[1];
					continue whileFile;
				}else {
					String operation = null;
					String file1 = splited[1]; 
					String file2 = null;
					if(splited[0].equals("A")) {
						operation = "ADDED";
					}else if(splited[0].equals("M")) {
						operation = "MODIFIED";
					}else if(splited[0].indexOf("R") != -1) {
						operation = "RENAMED";
						file2 = splited[2];
					}
					if(operation != null) {
						String line = null;
						if(file2 == null) {
							line = commitHash+";"+operation+"; ;"+file1;
						}else {
							line = commitHash+";"+operation+";"+file1+";"+file2;
						}
						lines.add(line);
					}
				}
			}
			java.io.File file = new java.io.File(projectPath+"commitfileinfo.log");
			FileOutputStream fos = new FileOutputStream(file);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (String line : lines) {
				bw.write(line);
				bw.newLine();
			}
			bw.close();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Commit> extractCommitsFileAndDiffsOfCommits(String projectPath, List<Commit> commits, List<File> files) {
		try {
			FileInputStream fstream = new FileInputStream(projectPath+Constants.diffFileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
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
						if (idCommitString.equals(commit.getExternalId())) {
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
										commitFile.setAdds(linesAdded);
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
										commitFile.setAdds(linesAdded);
									} catch (Exception e) {
										e.printStackTrace();
									}
									continue whileFile;
								}
							}
						}	
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("Error processing project diff "+e.getMessage());
					}
				}
			}
			br.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return commits;
	}

}
