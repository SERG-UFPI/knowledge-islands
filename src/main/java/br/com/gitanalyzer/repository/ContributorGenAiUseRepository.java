package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.ContributorGenAiUse;

@Repository
public interface ContributorGenAiUseRepository extends JpaRepository<ContributorGenAiUse, Long>{

}
