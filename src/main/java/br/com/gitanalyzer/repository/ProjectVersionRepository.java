package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.ProjectVersion;

@Repository
public interface ProjectVersionRepository extends JpaRepository<ProjectVersion, Long>{
	
	boolean existsByVersionId(String versionId);
	
	ProjectVersion findFirstByProjectIdOrderByDateVersionDesc(Long id);
	
	Long deleteByProjectId(Long id);
	
	void deleteByProjectIdIn(List<Long> ids);
	
}
