package br.com.gitanalyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gitanalyzer.repository.ProjectVersionRepository;

@Service
public class ProjectVersionService {

	@Autowired
	private ProjectVersionRepository repository;

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
}