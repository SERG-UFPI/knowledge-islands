package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.GitRepositoryVersionKnowledgeModelGenAi;

@Repository
public interface GitRepositoryVersionKnowledgeModelGenAiRepository extends JpaRepository<GitRepositoryVersionKnowledgeModelGenAi, Long>{

	boolean existsByAvgPctFilesGenAi(double avgPctFilesGenAi);
}

