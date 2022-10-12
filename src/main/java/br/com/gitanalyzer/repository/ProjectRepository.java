package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>{

	boolean existsByName(String name);
	boolean existsByNameAndVersion(String name, String version);

}
