package br.com.gitanalyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.extractors.HistoryCommitsExtractor;
import br.com.gitanalyzer.main.dto.HashNumberYears;
import br.com.gitanalyzer.model.entity.Project;
import br.com.gitanalyzer.repository.ProjectRepository;
import br.com.gitanalyzer.utils.ProjectUtils;

@Service
public class HistoryCommitsService {

	@Autowired
	private ProjectRepository projectRepository;
	private ProjectUtils projectUtils = new ProjectUtils();
	private HistoryCommitsExtractor extractor = new HistoryCommitsExtractor();

	public void commitsHashsFolder(HashNumberYears form) {
		java.io.File dir = new java.io.File(form.getPath());
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				String projectName = projectUtils.extractProjectName(projectPath);
				Project project = projectRepository.findByName(projectName);
				if(project.isFiltered() == false) {
					extractor.saveCommitsHashs(projectPath, form.getNumberYears());
				}
			}
		}
	}

	public void commitsHashsProject(HashNumberYears form) {
		String projectName = projectUtils.extractProjectName(form.getPath());
		Project project = projectRepository.findByName(projectName);
		if(project.isFiltered() == false) {
			extractor.saveCommitsHashs(form.getPath(), form.getNumberYears());
		}
	}

}
