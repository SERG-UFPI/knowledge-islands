package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.GitRepositoryVersionKnowledgeModel;

@Repository
public interface GitRepositoryVersionKnowledgeModelRepository extends JpaRepository<GitRepositoryVersionKnowledgeModel, Long>{
	//boolean existsByKnowledgeModelAndRepositoryVersionIdAndRootFolderPath(KnowledgeModel knowledgeModel, Long id, String rootFolderPath);
	List<GitRepositoryVersionKnowledgeModel> findByRepositoryVersionId(Long id);
}
