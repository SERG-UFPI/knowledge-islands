package br.com.knowledgeislands.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.knowledgeislands.dto.form.HashNumberYearsForm;
import br.com.knowledgeislands.extractors.HistoryCommitsExtractor;
import br.com.knowledgeislands.model.entity.GitRepository;
import br.com.knowledgeislands.repository.GitRepositoryRepository;

@Service
public class HistoryCommitsService {

	@Autowired
	private GitRepositoryRepository projectRepository;
	@Autowired
	private GitRepositoryService projectService;
	private HistoryCommitsExtractor extractor = new HistoryCommitsExtractor();

	public void commitsHashsFolder(HashNumberYearsForm form) {
		java.io.File dir = new java.io.File(form.getPath());
		for (java.io.File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String projectPath = fileDir.getAbsolutePath()+"/";
				String projectName = projectService.extractProjectName(projectPath);
				GitRepository project = projectRepository.findByName(projectName);
				if(project.isFiltered() == false) {
					extractor.saveCommitsHashs(projectPath, form.getNumberYears());
				}
			}
		}
	}

	public void commitsHashsProject(HashNumberYearsForm form) {
		String projectName = projectService.extractProjectName(form.getPath());
		GitRepository project = projectRepository.findByName(projectName);
		if(project.isFiltered() == false) {
			extractor.saveCommitsHashs(form.getPath(), form.getNumberYears());
		}
	}

}
