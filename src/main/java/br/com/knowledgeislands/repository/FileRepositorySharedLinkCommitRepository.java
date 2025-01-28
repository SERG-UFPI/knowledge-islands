package br.com.knowledgeislands.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.FileRepositorySharedLinkCommit;
import br.com.knowledgeislands.model.entity.GitRepository;

@Repository
public interface FileRepositorySharedLinkCommitRepository extends JpaRepository<FileRepositorySharedLinkCommit, Long>{
	@Query("SELECT DISTINCT fg.gitRepository FROM FileRepositorySharedLinkCommit fg " +
			"JOIN fg.sharedLinksCommits sl " +
			"WHERE sl.sharedLink.conversation IS NOT NULL")
	List<GitRepository> findDistinctGitRepositoriesWithNonNullConversation();

	@Query("SELECT DISTINCT fg.gitRepository FROM FileRepositorySharedLinkCommit fg " +
			"JOIN fg.sharedLinksCommits sl " +
			"WHERE sl.sharedLink.conversation IS NOT NULL and fg.gitRepository.cloneUrl is NOT NULL and fg.gitRepository.currentFolderPath is NULL")
	List<GitRepository> findDistinctGitRepositoriesWithNonNullConversationAndCloneUrlNotNullAndCurrentFolderPathIsNull();

	@Query("SELECT DISTINCT fg.gitRepository FROM FileRepositorySharedLinkCommit fg " +
			"JOIN fg.sharedLinksCommits sl " +
			"WHERE sl.sharedLink.conversation IS NOT NULL and fg.gitRepository.currentFolderPath is not NULL")
	List<GitRepository> findDistinctGitRepositoriesWithNonNullConversationAndCurrentFolderPathIsNotNull();

	@Query("SELECT fg FROM FileRepositorySharedLinkCommit fg " +
			"JOIN fg.sharedLinksCommits sl " +
			"WHERE sl.sharedLink.conversation IS NOT NULL")
	List<FileRepositorySharedLinkCommit> findWithNonNullConversation();

	List<FileRepositorySharedLinkCommit> findByGitRepositoryId(Long id);

	FileRepositorySharedLinkCommit findByFileIdAndGitRepositoryId(Long fileId, Long gitRepositoryId);

	@Query("SELECT frslc " +
			"FROM FileRepositorySharedLinkCommit frslc " +
			"WHERE frslc.file.id = :fileId " +
			"AND frslc.gitRepository.id = :gitRepositoryId " +
			"AND frslc.id <> :excludeId")
	List<FileRepositorySharedLinkCommit> findByFileAndGitRepositoryExcludingId(@Param("fileId") Long fileId, @Param("gitRepositoryId") Long gitRepositoryId, 
			@Param("excludeId") Long excludeId);

	@Query("SELECT frslc " +
			"FROM FileRepositorySharedLinkCommit frslc " +
			"WHERE frslc.file IN (" +
			"    SELECT frslcInner.file " +
			"    FROM FileRepositorySharedLinkCommit frslcInner " +
			"    GROUP BY frslcInner.file " +
			"    HAVING COUNT(frslcInner.id) > 1" +
			")")
	List<FileRepositorySharedLinkCommit> findEntitiesWithDuplicateFiles();
}
