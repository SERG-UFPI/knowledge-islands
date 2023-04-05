package br.com.gitanalyzer.service;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import br.com.gitanalyzer.extractors.ProjectVersionExtractor;
import br.com.gitanalyzer.model.Project;
import br.com.gitanalyzer.model.ProjectVersion;
import br.com.gitanalyzer.repository.ProjectRepository;
import br.com.gitanalyzer.repository.ProjectVersionRepository;
import br.com.gitanalyzer.utils.ProjectUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProjectService {

	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private ProjectVersionRepository projectVersionRepository; 

	public Object setProjectsMainLanguage() {
		HashMap<String, String> nameLanguage = new HashMap<String, String>();
		List<Project> projetos = projectRepository.findAll();
		List<String[]> projectsFile = null;
		try (CSVReader reader = new CSVReader(new FileReader("/home/otavio/Desktop/shell_scripts/rep-info-new.csv"))) {
			projectsFile = reader.readAll();
		} catch (IOException | CsvException e) {
			e.printStackTrace();
		}
		for (String[] string : projectsFile) {
			String name = string[0];
			String language = string[1];
			nameLanguage.put(name, language);
		}
		for (Project project : projetos) {
			for(Map.Entry<String, String> set: nameLanguage.entrySet()) {
				if (set.getKey().contains(project.getName())) {
					project.setMainLanguage(set.getValue());
					break;
				}
			}
			projectRepository.save(project);
		}
		return null;
	}

	public void extractVersion(String folderPath) {
		ProjectUtils projectUtils = new ProjectUtils();
		ProjectVersionExtractor projectVersionExtractor = new ProjectVersionExtractor();
		java.io.File dir = new java.io.File(folderPath);
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				String projectName = projectUtils.extractProjectName(projectPath);
				Project project = projectRepository.findByName(projectName);
				log.info("EXTRACTING DATA FROM "+projectName);
				ProjectVersion version = projectVersionExtractor
						.extractProjectVersionOnlyNumbers(projectPath);
				project.setFirstCommitDate(version.getFirstCommitDate());
				version.setProject(project);
				projectVersionRepository.save(version);
				log.info("EXTRACTION FINISHED");
			}
		}
	}


}
