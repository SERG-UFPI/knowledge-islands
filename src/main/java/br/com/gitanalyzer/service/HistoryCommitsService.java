package br.com.gitanalyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.extractors.HistoryCommitsExtractor;
import br.com.gitanalyzer.main.dto.HashNumberYears;
import br.com.gitanalyzer.model.Project;
import br.com.gitanalyzer.repository.ProjectRepository;
import br.com.gitanalyzer.utils.ProjectUtils;

@Service
public class HistoryCommitsService {
	
	@Autowired
	private ProjectRepository projectRepository;

	public void commitsHashs(HashNumberYears form) {
		HistoryCommitsExtractor extractor = new HistoryCommitsExtractor();
		ProjectUtils projectUtils = new ProjectUtils();
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
	
}
