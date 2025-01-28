package br.com.knowledgeislands.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.PromptCode;

@Repository
public interface PromptCodeRepository extends JpaRepository<PromptCode, Long> {

}
