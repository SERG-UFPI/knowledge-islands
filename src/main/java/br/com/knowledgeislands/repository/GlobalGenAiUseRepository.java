package br.com.knowledgeislands.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.GlobalGenAiUse;

@Repository
public interface GlobalGenAiUseRepository extends JpaRepository<GlobalGenAiUse, Long> {

}
