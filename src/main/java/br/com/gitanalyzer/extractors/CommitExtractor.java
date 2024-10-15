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

import br.com.gitanalyzer.model.entity.Commit;
import br.com.gitanalyzer.utils.KnowledgeIslandsUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CommitExtractor {

	private List<Date> getDatesOfProject(String projectPath) throws IOException{
		List<Date> dates = new ArrayList<Date>();
		FileInputStream fstream = new FileInputStream(projectPath+KnowledgeIslandsUtils.commitFileName);
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
		if(!dates.isEmpty()) {
			Collections.sort(dates);
			return dates.get(dates.size()-1);
		}else {
			return null;
		}
	}

	public Date getFirstCommitDate(String projectPath) throws IOException {
		List<Date> dates = getDatesOfProject(projectPath);
		if(!dates.isEmpty()) {
			Collections.sort(dates);
			return dates.get(0);
		}else {
			return null;
		}
	}

	public String getLastCommitHash(String projectPath) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(projectPath+KnowledgeIslandsUtils.commitFileName));
		String hash = br.readLine().split(";")[0];
		br.close();
		return hash;
	}

	public List<Commit> getCommitsDatesAndHashes(String projectPath){
		List<Commit> commits = new ArrayList<>();
		try {
			FileInputStream fstream = new FileInputStream(projectPath+KnowledgeIslandsUtils.commitFileName);
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
			return new ArrayList<>();
		}
	}

	public void generateCommitFileFile(String projectPath) {
		try {
			FileInputStream fstream = new FileInputStream(projectPath+KnowledgeIslandsUtils.logFile);
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

}
