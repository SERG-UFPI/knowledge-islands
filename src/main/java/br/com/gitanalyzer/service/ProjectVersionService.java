package br.com.gitanalyzer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gitanalyzer.model.entity.Project;
import br.com.gitanalyzer.repository.ProjectRepository;
import br.com.gitanalyzer.repository.ProjectVersionRepository;

@Service
public class ProjectVersionService {

	@Autowired
	private ProjectVersionRepository repository;
	@Autowired
	private ProjectRepository projectRepository;

	public void remove(Long id) {
		repository.deleteById(id);
	}

	public void removeAll() {
		repository.deleteAll();
	}

	@Transactional
	public void removeFromProject(Long id) {
		repository.deleteByProjectId(id);
	}

	@Transactional
	public void removeFromProjectsFiltered() {
		List<Project> projects = projectRepository.findByFilteredTrue();
		List<Long> ids = projects.stream().map(p -> p.getId()).toList();
		repository.deleteByProjectIdIn(ids);
	}

}