package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.enums.KnowledgeMetric;
import br.com.gitanalyzer.model.TruckFactor;

@Repository
public interface TruckFactorRepository extends JpaRepository<TruckFactor, Long> {

	boolean existsByKnowledgeMetricAndProjectId(KnowledgeMetric knowledgeMetric, Long id);
}
