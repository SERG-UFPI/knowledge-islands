package br.com.gitanalyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.dto.form.HashNumberYearsForm;
import br.com.gitanalyzer.extractors.HistoryCommitsExtractor;
import br.com.gitanalyzer.model.entity.Project;
import br.com.gitanalyzer.repository.ProjectRepository;

@Service
public class HistoryCommitsService {

	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private ProjectService projectService;
	private HistoryCommitsExtractor extractor = new HistoryCommitsExtractor();

	public void commitsHashsFolder(HashNumberYearsForm form) {
		java.io.File dir = new java.io.File(form.getPath());
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				String projectName = projectService.extractProjectName(projectPath);
				Project project = projectRepository.findByName(projectName);
				if(project.isFiltered() == false) {
					extractor.saveCommitsHashs(projectPath, form.getNumberYears());
				}
			}
		}
	}

	public void commitsHashsProject(HashNumberYearsForm form) {
		String projectName = projectService.extractProjectName(form.getPath());
		Project project = projectRepository.findByName(projectName);
		if(project.isFiltered() == false) {
			extractor.saveCommitsHashs(form.getPath(), form.getNumberYears());
		}
	}

}
