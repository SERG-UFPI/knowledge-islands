package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.GitRepositoryFolder;

@Repository
public interface GitRepositoryFolderRepository extends JpaRepository<GitRepositoryFolder, Long>{
	List<GitRepositoryFolder> findByFolderTrue();
}
