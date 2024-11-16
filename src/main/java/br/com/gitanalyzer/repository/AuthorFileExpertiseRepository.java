package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.AuthorFileExpertise;

@Repository
public interface AuthorFileExpertiseRepository extends JpaRepository<AuthorFileExpertise, Long> {

	@Query("select afe from AuthorFileExpertise afe where afe.contributorVersion.contributor.id = ?1 and afe.fileVersion.file.id = ?2")
	List<AuthorFileExpertise> findByAuthorAndFile(Long idContributor, Long idFile);
}
