package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.TruckFactor;

@Repository
public interface TruckFactorRepository extends JpaRepository<TruckFactor, Long> {
	
	@Query("select truckFactor.gitRepositoryVersionKnowledgeModel.repositoryVersion.gitRepository.id from TruckFactor truckFactor")
	List<Long> findRepositoriesIds();
}
