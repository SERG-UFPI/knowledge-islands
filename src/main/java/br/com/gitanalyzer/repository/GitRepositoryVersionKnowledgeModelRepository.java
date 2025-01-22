package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.GitRepositoryVersionKnowledgeModel;
import br.com.gitanalyzer.model.enums.KnowledgeModel;

@Repository
public interface GitRepositoryVersionKnowledgeModelRepository extends JpaRepository<GitRepositoryVersionKnowledgeModel, Long>{
	//boolean existsByKnowledgeModelAndRepositoryVersionIdAndRootFolderPath(KnowledgeModel knowledgeModel, Long id, String rootFolderPath);
	List<GitRepositoryVersionKnowledgeModel> findByRepositoryVersionId(Long id);
	@Query("select model.id from GitRepositoryVersionKnowledgeModel model")
	List<Long> findAllIds();
	@Query("select model.id from GitRepositoryVersionKnowledgeModel model where model.truckFactor is null")
	List<Long> findIdByTruckFactorIsNull();
	boolean existsByRepositoryVersionIdAndKnowledgeModelAndGitRepositoryVersionKnowledgeModelGenAiAvgPctFilesGenAi(Long gitRepositoryVersionId, 
			KnowledgeModel model, double avgPctFilesGenAi);
	boolean existsByRepositoryVersionIdAndKnowledgeModelAndGitRepositoryVersionKnowledgeModelGenAiIsNull(Long gitRepositoryVersionId, KnowledgeModel model);
}
