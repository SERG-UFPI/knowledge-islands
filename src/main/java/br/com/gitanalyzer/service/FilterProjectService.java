package br.com.gitanalyzer.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.extractors.ProjectVersionExtractor;
import br.com.gitanalyzer.model.Project;
import br.com.gitanalyzer.model.ProjectVersion;
import br.com.gitanalyzer.repository.ProjectRepository;
import br.com.gitanalyzer.utils.ProjectUtils;

@Service
public class FilterProjectService {
	
	@Autowired
	private ProjectRepository projectRepository;
	
	public void filter(String path) {
		List<ProjectVersion> versions = new ArrayList<ProjectVersion>();
		ProjectUtils projectUtils = new ProjectUtils();
		ProjectVersionExtractor projectVersionExtractor = new ProjectVersionExtractor();
		java.io.File dir = new java.io.File(path);
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				String projectName = projectUtils.extractProjectName(projectPath);
				Project project = projectRepository.findByName(projectName);
				ProjectVersion version = projectVersionExtractor
						.extractProjectVersionOnlyNumbers(projectPath);
				project.setFirstCommitDate(version.getFirstCommitDate());
				version.setProject(project);
				versions.add(version);
				
			}
		}
		List<Project> projectsFiltered = new ArrayList<Project>();
		Map<String, List<ProjectVersion>> versionMap = versions.stream().collect(Collectors.groupingBy(ProjectVersion::getProjectLanguage));
		for(var entry: versionMap.entrySet()) {
			projectsFiltered.addAll(filterProjectBySize(entry.getValue()));
		}
		for (Project project : projectsFiltered) {
			project.setFiltered(true);
			projectRepository.save(project);
		}
	}

	private List<Project> filterProjectBySize(List<ProjectVersion> versions) {
		List<Double> devs = versions.stream().map(v -> Double.valueOf(v.getNumberAnalysedDevs())).toList();
		List<Double> commits = versions.stream().map(v -> Double.valueOf(v.getNumberAllCommits())).toList();
		List<Double> files = versions.stream().map(v -> Double.valueOf(v.getNumberAllFiles())).toList();
		double[] devsArray = new double[devs.size()];
		double[] commitsArray = new double[commits.size()];
		double[] filesArray = new double[files.size()];
		
		Percentile p = new Percentile();
		double firstQDevs = p.evaluate(devsArray, 25);
		double firstQCommits = p.evaluate(commitsArray, 25);
		double firstQFiles = p.evaluate(filesArray, 25);
		Set<Project> projects = new HashSet<Project>();
		projects.addAll(versions.stream().filter(v -> v.getNumberAnalysedDevs() < firstQDevs).map(v -> v.getProject()).toList());
		projects.addAll(versions.stream().filter(v -> v.getNumberAllCommits() < firstQCommits).map(v -> v.getProject()).toList());
		projects.addAll(versions.stream().filter(v -> v.getNumberAllFiles() < firstQFiles).map(v -> v.getProject()).toList());
		return new ArrayList<>(projects);
	}

}
