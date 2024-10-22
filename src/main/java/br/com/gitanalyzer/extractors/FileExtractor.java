package br.com.gitanalyzer.extractors;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.utils.KnowledgeIslandsUtils;

public class FileExtractor {

	public int extractSizeAllFiles(String path) {
		int lines = 0;
		try {
			FileInputStream fstream = new FileInputStream(path+KnowledgeIslandsUtils.allFilesFileName);
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

}
