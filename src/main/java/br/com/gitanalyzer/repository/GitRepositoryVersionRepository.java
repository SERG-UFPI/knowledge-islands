package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.GitRepositoryVersion;

@Repository
public interface GitRepositoryVersionRepository extends JpaRepository<GitRepositoryVersion, Long>{
	
	boolean existsByVersionIdAndGitRepositoryId(String versionId, Long id);
	
	GitRepositoryVersion findFirstByGitRepositoryIdOrderByDateVersionDesc(Long id);
	
	Long deleteByGitRepositoryId(Long id);
	
	void deleteByGitRepositoryIdIn(List<Long> ids);
	
}
