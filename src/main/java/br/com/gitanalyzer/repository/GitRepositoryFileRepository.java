package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.GitRepositoryFile;

@Repository
public interface GitRepositoryFileRepository extends JpaRepository<GitRepositoryFile, Long>{
	List<GitRepositoryFile> findByFileLanguage(String language);
	@Query("SELECT DISTINCT grf FROM GitRepositoryFile grf " +
			"JOIN SharedLink sl ON sl MEMBER OF grf.sharedLinks " +
			"WHERE sl.conversation IS NOT NULL")
	List<GitRepositoryFile> findAllWithSharedLinkConversationNotNull();
	
	@Query("SELECT DISTINCT grf FROM GitRepositoryFile grf " +
			"JOIN SharedLink sl ON sl MEMBER OF grf.sharedLinks " +
			"WHERE sl.conversation IS NOT NULL and grf.file.language = :language")
	List<GitRepositoryFile> findAllWithSharedLinkConversationNotNullByLanguage(@Param("language") String language);
}
