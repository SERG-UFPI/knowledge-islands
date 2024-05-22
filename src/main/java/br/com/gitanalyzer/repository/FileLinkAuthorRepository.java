package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.github_openai.FileLinkAuthor;

@Repository
public interface FileLinkAuthorRepository extends JpaRepository<FileLinkAuthor, Long> {

	boolean existsBySharedLinkLinkAndSharedLinkRepositoryFullNameAndAuthorFileFileVersionFileName(String link, String repoFullName, String fileName);
}
