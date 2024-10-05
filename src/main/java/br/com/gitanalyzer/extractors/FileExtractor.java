package br.com.gitanalyzer.extractors;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;

import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.enums.OperationType;
import br.com.gitanalyzer.utils.Constants;

public class FileExtractor {

	public int extractSizeAllFiles(String path) {
		int lines = 0;
		try {
			FileInputStream fstream = new FileInputStream(path+Constants.allFilesFileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if(!"".equals(strLine.trim())){
					lines++;
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	public List<File> extractFileList(String path, String fileListName, String projectName) {
		try {
			Map<String, String[]> projectPatterns  = new HashMap<>();
			String arrayLinux[]  = new String[] {"drivers/", "crypto/", "sound/", "security/"};
			String arrayHomebrew[]  = new String[] {"Library/Formula/"};
			String arrayHomebrewCask[]  = new String[] {"Casks/"};

			List<File> files = new ArrayList<File>();
			String fileListfullPath = path+fileListName;
			projectPatterns.put("linux", arrayLinux);
			projectPatterns.put("homebrew", arrayHomebrew);
			projectPatterns.put("homebrew-cask", arrayHomebrewCask);
			String patterns[] = null;
			if (projectPatterns.containsKey(projectName)) {
				patterns = projectPatterns.get(projectName);
			}
			FileInputStream fstream = new FileInputStream(fileListfullPath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			whileFile:while ((strLine = br.readLine()) != null) {
				String[] splited = strLine.split(";");
				String filePath = null;
				if(splited.length == 1) {
					filePath = splited[0];
				}else {
					filePath = splited[1];
				}
				if (patterns == null) {
					files.add(new File(filePath));
				}else {
					for (String startPattern : patterns) {
						if (filePath.startsWith(startPattern) == true) {
							continue whileFile;
						}
					}
					files.add(new File(filePath));
				}
			}
			br.close();
			return files;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<File> extractFileSizeList(String path, String fileListName, 
			String clocFileName, String projectName){
		try {
			List<File> files = extractFileList(path, fileListName, projectName);
			String strLineCloc;
			String clocListfullPath = path+clocFileName;
			FileInputStream fstreamCloc = new FileInputStream(clocListfullPath);
			BufferedReader brCloc = new BufferedReader(new InputStreamReader(fstreamCloc));
			while ((strLineCloc = brCloc.readLine()) != null) {
				String filePathCloc = strLineCloc.split(";")[0];
				for (File file : files) {
					if (filePathCloc.equals(file.getPath())) {
						if (strLineCloc.split(";").length > 1) {
							String fileSizeString = strLineCloc.split(";")[2];
							if (fileSizeString != null && fileSizeString.equals("") == false) {
								file.setSize(Integer.parseInt(fileSizeString));
								break;
							}
						}
					}
				}
			}
			brCloc.close();
			files.removeIf(f -> f.getSize() == 0);
			return files;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	public List<File> extractFilesFromClocFile(String path, String projectName) {
		Map<String, String[]> projectPatterns  = new HashMap<>();
		String arrayLinux[]  = new String[] {"drivers/", "crypto/", "sound/", "security/"};
		String arrayHomebrew[]  = new String[] {"Library/Formula/"};
		String arrayHomebrewCask[]  = new String[] {"Casks/"};
		projectPatterns.put("linux", arrayLinux);
		projectPatterns.put("homebrew", arrayHomebrew);
		projectPatterns.put("homebrew-cask", arrayHomebrewCask);
		String patterns[] = null;
		if (projectPatterns.containsKey(projectName)) {
			patterns = projectPatterns.get(projectName);
		}
		List<File> files = new ArrayList<File>();
		List<File> filesWithoutSize = new ArrayList<File>();
		String strLineCloc;
		String clocListPath = path+Constants.clocFileName;
		try {
			FileInputStream fstreamCloc = new FileInputStream(clocListPath);
			BufferedReader brCloc = new BufferedReader(new InputStreamReader(fstreamCloc));
			whileFile: while ((strLineCloc = brCloc.readLine()) != null) {
				String[] splitedLine = strLineCloc.split(";");
				String filePath = splitedLine[0];
				if (patterns != null) {
					for (String startPattern : patterns) {
						if (filePath.startsWith(startPattern) == true) {
							continue whileFile;
						}
					}
				}
				if (splitedLine.length > 1) {
					String fileSizeString = splitedLine[2];
					if (fileSizeString != null && !fileSizeString.equals("")) {
						if(!fileSizeString.equals("0")) {
							int size = Integer.parseInt(fileSizeString);
							File file = new File(filePath, size);
							files.add(file);
						}
					}else {
						filesWithoutSize.add(new File(filePath));
					}
				}else {
					filesWithoutSize.add(new File(filePath));
				}
			}
			brCloc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(!filesWithoutSize.isEmpty()) {
			Git git = null;
			Repository repository = null;
			try {
				git = Git.open(new java.io.File(path));
				repository = git.getRepository();
				for (File file : filesWithoutSize) {
					if (file.getSize() == 0) {
						BlameCommand blameCommand = new BlameCommand(repository);
						blameCommand.setTextComparator(RawTextComparator.WS_IGNORE_ALL);
						blameCommand.setFilePath(file.getPath());
						BlameResult blameResult = blameCommand.call();
						RawText rawText = blameResult.getResultContents();
						file.setSize(rawText.size());
						if(file.getSize() > 0) {
							files.add(file);
						}
					}
				}
			} catch (IOException | GitAPIException e1) {
				e1.printStackTrace();
			}
		}
		return files;
	}

	public void getRenamesFiles(String projectPath, List<File> files) {
		HashMap<String, String> newOldName = new HashMap<String, String>();
		try {
			FileInputStream fstream = new FileInputStream(projectPath+Constants.commitFileFileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] splited = strLine.split(";");
				String operation = splited[1];
				if (operation.equals(OperationType.RENAMED.name())) {
					String oldPath = splited[2];
					String fileName = splited[3];
					newOldName.put(oldPath, fileName);
				}
			}
			br.close();
			for (File file : files) {
				for (Map.Entry<String, String> entry : newOldName.entrySet()) {
					String fileName = entry.getValue();
					if (file.isFile(fileName)) {
						file.getRenamePaths().add(entry.getKey());
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getBlameOfFile(GitRepository repository, File file) {

	}
}
