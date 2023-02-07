package br.com.gitanalyzer.main.filter;

import br.com.gitanalyzer.extractors.ProjectVersionExtractor;
import br.com.gitanalyzer.model.ProjectVersion;
import br.com.gitanalyzer.utils.ProjectUtils;

public class FilterProjects {
	
	public static void main(String[] args) {
		ProjectUtils projectUtils = new ProjectUtils();
		ProjectVersionExtractor projectVersionExtractor = new ProjectVersionExtractor();
		String path = args[0];
		java.io.File dir = new java.io.File(path);
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				String projectName = projectUtils.extractProjectName(projectPath);
				ProjectVersion projectVersion = projectVersionExtractor
						.extractProjectVersionOnlyNumbers(projectPath, projectName);
				
			}
		}
	}

}
