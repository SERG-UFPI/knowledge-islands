package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.Contributor;

@Repository
public interface ContributorRepository extends JpaRepository<Contributor, Long>{

	boolean existsByEmail(String email);
	@Query("SELECT DISTINCT cf.commit.author FROM CommitFile cf where cf.removingsCodes > 0 and cf.commit.author.emailSharedLinkSent is false")
	List<Contributor> findContributorFromCommitFilesWithRemovedCodes();
}
