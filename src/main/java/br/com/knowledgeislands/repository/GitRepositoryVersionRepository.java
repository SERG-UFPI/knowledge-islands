package br.com.knowledgeislands.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.GitRepositoryVersion;

@Repository
public interface GitRepositoryVersionRepository extends JpaRepository<GitRepositoryVersion, Long>{

	boolean existsByVersionIdAndGitRepositoryId(String versionId, Long id);

	GitRepositoryVersion findFirstByGitRepositoryIdOrderByDateVersionDesc(Long id);

	Long deleteByGitRepositoryId(Long id);

	void deleteByGitRepositoryIdIn(List<Long> ids);

	List<GitRepositoryVersion> findByGitRepositoryId(Long id);
	
	@Query("SELECT g FROM GitRepositoryVersion g JOIN FETCH g.commits")
	List<GitRepositoryVersion> findAllWithCommits();

}
