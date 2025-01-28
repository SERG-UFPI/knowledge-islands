package br.com.knowledgeislands.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.GitRepositoryVersionKnowledgeModelGenAi;

@Repository
public interface GitRepositoryVersionKnowledgeModelGenAiRepository extends JpaRepository<GitRepositoryVersionKnowledgeModelGenAi, Long>{

	GitRepositoryVersionKnowledgeModelGenAi findByAvgPctFilesGenAi(double avgPctFilesGenAi);
}

