package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.AuthorFile;

@Repository
public interface AuthorFileRepository extends JpaRepository<AuthorFile, Long> {

}
