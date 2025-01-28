package br.com.knowledgeislands.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.GitRepository;

@Repository
public interface GitRepositoryRepository extends JpaRepository<GitRepository, Long>{

	boolean existsByName(String name);
	GitRepository findByName(String name);
	List<GitRepository> findByFilteredTrue();
	List<GitRepository> findByFilteredFalse();
	GitRepository findByFullName(String fullName);
	List<GitRepository> findByFullNameIn(List<String> fullNames);
	List<GitRepository> findByNameEndingWith(String suffix);
	GitRepository findByCurrentFolderPath(String currentFolderPath);
	boolean existsByCurrentFolderPath(String path);

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
