package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.FileGitRepositorySharedLinkCommit;
import br.com.gitanalyzer.model.entity.GitRepository;

@Repository
public interface FileGitRepositorySharedLinkCommitRepository extends JpaRepository<FileGitRepositorySharedLinkCommit, Long>{
	@Query("SELECT DISTINCT fg.gitRepository FROM FileGitRepositorySharedLinkCommit fg " +
			"JOIN fg.sharedLinksCommits sl " +
			"WHERE sl.sharedLink.conversation IS NOT NULL")
	List<GitRepository> findDistinctGitRepositoriesWithNonNullConversation();

	@Query("SELECT DISTINCT fg.gitRepository FROM FileGitRepositorySharedLinkCommit fg " +
			"JOIN fg.sharedLinksCommits sl " +
			"WHERE sl.sharedLink.conversation IS NOT NULL and fg.gitRepository.cloneUrl is NOT NULL and fg.gitRepository.currentFolderPath is NULL")
	List<GitRepository> findDistinctGitRepositoriesWithNonNullConversationAndCloneUrlNotNullAndCurrentFolderPathIsNull();

	List<FileGitRepositorySharedLinkCommit> findByGitRepositoryId(Long id);
}
