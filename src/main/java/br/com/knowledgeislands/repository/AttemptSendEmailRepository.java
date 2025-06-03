package br.com.knowledgeislands.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.AttemptSendEmail;

@Repository
public interface AttemptSendEmailRepository extends JpaRepository<AttemptSendEmail, Long>{

	boolean existsByContributorId(Long id);

}
