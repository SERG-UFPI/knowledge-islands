package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.entity.SharedLinkCommit;

@Repository
public interface SharedLinkCommitRepository extends JpaRepository<SharedLinkCommit, Long>{

	List<SharedLinkCommit> findByCommitFileAddedLinkIsNotNull();

	@Query("SELECT DISTINCT slc.fileRepositorySharedLinkCommit.gitRepository FROM SharedLinkCommit slc " +
			"WHERE slc.commitFileAddedLink IS NOT NULL")
	List<GitRepository> findRepositoriesBySharedLinkCommitWithCommitFile();

}
