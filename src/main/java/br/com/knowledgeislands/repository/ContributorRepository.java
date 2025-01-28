package br.com.knowledgeislands.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.Contributor;

@Repository
public interface ContributorRepository extends JpaRepository<Contributor, Long>{

	boolean existsByEmail(String email);
	@Query("SELECT DISTINCT slc.commitFileAddedLink.commit.author FROM SharedLinkCommit slc where slc.commitFileAddedLink != null and slc.numberCopiedLines > 0")
	List<Contributor> findContributorFromCommitFilesWithCopiedLines();
	@Query("select c from Contributor c where not exists (select cgu from ContributorGenAiUse cgu where cgu.contributor.id = c.id)")
	List<Contributor> findContributorNotExistsContributorGenAiUse();
}
