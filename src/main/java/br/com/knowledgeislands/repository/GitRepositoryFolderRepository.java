package br.com.knowledgeislands.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.GitRepositoryFolder;

@Repository
public interface GitRepositoryFolderRepository extends JpaRepository<GitRepositoryFolder, Long>{
	List<GitRepositoryFolder> findByFolderTrue();
}
