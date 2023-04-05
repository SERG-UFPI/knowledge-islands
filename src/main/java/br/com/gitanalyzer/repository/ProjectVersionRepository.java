package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.ProjectVersion;

@Repository
public interface ProjectVersionRepository extends JpaRepository<ProjectVersion, Long>{
	
	boolean existsByVersionId(String versionId);
	
	ProjectVersion findFirstByProjectIdOrderByDateVersionDesc(Long id);
	
}
