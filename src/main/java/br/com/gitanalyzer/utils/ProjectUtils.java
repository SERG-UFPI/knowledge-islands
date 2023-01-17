package br.com.gitanalyzer.utils;

import java.io.File;

public class ProjectUtils {

	public String extractProjectName(String path) {
		String fileSeparator = File.separator;
		String[] splitedPath = path.split("\\"+fileSeparator);
		String projectName = splitedPath[splitedPath.length - 1];
		return projectName;
	}

}
