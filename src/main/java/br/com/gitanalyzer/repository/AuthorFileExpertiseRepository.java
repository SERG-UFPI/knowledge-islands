package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.AuthorFileExpertise;

@Repository
public interface AuthorFileExpertiseRepository extends JpaRepository<AuthorFileExpertise, Long> {

}
