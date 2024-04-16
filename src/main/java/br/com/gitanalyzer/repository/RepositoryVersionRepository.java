package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.GitRepositoryVersion;

@Repository
public interface RepositoryVersionRepository extends JpaRepository<GitRepositoryVersion, Long>{
	
	boolean existsByVersionId(String versionId);
	
	GitRepositoryVersion findFirstByRepositoryIdOrderByDateVersionDesc(Long id);
	
	Long deleteByRepositoryId(Long id);
	
	void deleteByRepositoryIdIn(List<Long> ids);
	
}
