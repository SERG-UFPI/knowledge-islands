package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.GitRepositoryFile;

@Repository
public interface GitRepositoryFileRepository extends JpaRepository<GitRepositoryFile, Long>{
	
}
