package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.enums.KnowledgeMetric;
import br.com.gitanalyzer.model.TruckFactorDevelopers;

@Repository
public interface TruckFactorDevelopersRepository extends JpaRepository<TruckFactorDevelopers, Long> {

	boolean existsByKnowledgeMetricAndTruckFactorIdAndNameAndEmail(KnowledgeMetric knowledgeMetric, Long id, 
			String name, String email);
}
