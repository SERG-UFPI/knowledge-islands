package br.com.knowledgeislands.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.GitRepository;
import br.com.knowledgeislands.model.entity.SharedLinkCommit;

@Repository
public interface SharedLinkCommitRepository extends JpaRepository<SharedLinkCommit, Long>{

	List<SharedLinkCommit> findByCommitFileAddedLinkIsNotNull();

	@Query("SELECT DISTINCT slc.fileRepositorySharedLinkCommit.gitRepository FROM SharedLinkCommit slc " +
			"WHERE slc.commitFileAddedLink IS NOT NULL")
	List<GitRepository> findRepositoriesBySharedLinkCommitWithCommitFile();
	
	@Query("SELECT DISTINCT slc.fileRepositorySharedLinkCommit.gitRepository FROM SharedLinkCommit slc " +
			"WHERE slc.commitFileAddedLink IS NOT NULL and slc.fileRepositorySharedLinkCommit.gitRepository.filtered is false")
	List<GitRepository> findRepositoriesNotFilteredBySharedLinkCommitWithCommitFile();
	
	@Query("SELECT slc from SharedLinkCommit slc where slc.commitFileAddedLink IS NOT NULL and slc.numberCopiedLines > 0")
	List<SharedLinkCommit> findSharedLinkWithCopiedLines();
	
	@Query("SELECT slc from SharedLinkCommit slc where slc.commitFileAddedLink IS NOT NULL and slc.numberCopiedLines > 0 and slc.maxLengthCopiedLines > 1")
	List<SharedLinkCommit> findSharedLinkWithCopiedLinesMoreThanOne();

}
