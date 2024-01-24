package br.com.gitanalyzer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gitanalyzer.model.entity.Project;
import br.com.gitanalyzer.model.entity.ProjectVersion;
import br.com.gitanalyzer.repository.ProjectRepository;
import br.com.gitanalyzer.repository.ProjectVersionRepository;
import br.com.gitanalyzer.utils.AsyncUtils;

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
		List<ProjectVersion> versions = repository.findAll();
		List<Long> ids = versions.stream().map(v -> v.getId()).toList();

		ExecutorService executorService = AsyncUtils.getExecutorServiceForLogs();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (Long id : ids) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
				repository.deleteById(id);
			}, executorService);
			futures.add(future);
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		executorService.shutdown();
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