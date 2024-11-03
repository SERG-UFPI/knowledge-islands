package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.GitRepository;

@Repository
public interface GitRepositoryRepository extends JpaRepository<GitRepository, Long>{

	boolean existsByName(String name);
	GitRepository findByName(String name);
	List<GitRepository> findByFilteredTrue();
	GitRepository findByFullName(String fullName);
	List<GitRepository> findByNameEndingWith(String suffix);
	GitRepository findByCurrentFolderPath(String currentFolderPath);

	@Query("SELECT DISTINCT gr FROM GitRepository gr " +
			"JOIN FileRepositorySharedLinkCommit grf ON grf.gitRepository = gr " +
			"JOIN SharedLink sl ON sl MEMBER OF grf.sharedLinksCommits " +
			"WHERE sl.conversation IS NOT NULL and gr.cloneUrl is NULL")
	List<GitRepository> findAllWithSharedLinkConversationNotNullAndCloneUrlIsNull();

	@Query("SELECT DISTINCT gr FROM GitRepository gr " +
			"JOIN FileRepositorySharedLinkCommit grf ON grf.gitRepository = gr " +
			"JOIN SharedLink sl ON sl MEMBER OF grf.sharedLinksCommits " +
			"WHERE sl.conversation IS NOT NULL")
	List<GitRepository> findAllWithSharedLinkConversationNotNull();

	@Query("SELECT DISTINCT gr FROM GitRepository gr " +
			"JOIN FileRepositorySharedLinkCommit grf ON grf.gitRepository = gr " +
			"JOIN SharedLink sl ON sl MEMBER OF grf.sharedLinksCommits " +
			"WHERE sl.conversation IS NOT NULL and gr.cloneUrl is NOT NULL and gr.currentFolderPath is NULL")
	List<GitRepository> findAllWithSharedLinkConversationNotNullAndCloneUrlNotNullAndCurrentFolderPathIsNull();

}
