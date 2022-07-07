package br.com.gitanalyzer.extractors;

import java.io.File;

public class ProjectExtractor {

	public String extractProjectName(String path) {
		String fileSeparator = File.separator;
		String[] splitedPath = path.split("\\"+fileSeparator);
		String projectName = splitedPath[splitedPath.length - 1];
		return projectName;
	}

}
