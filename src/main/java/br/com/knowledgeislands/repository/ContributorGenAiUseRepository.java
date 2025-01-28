package br.com.knowledgeislands.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.ContributorGenAiUse;

@Repository
public interface ContributorGenAiUseRepository extends JpaRepository<ContributorGenAiUse, Long>{

}
