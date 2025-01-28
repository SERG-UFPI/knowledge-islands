package br.com.knowledgeislands.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.Commit;

@Repository
public interface CommitRepository extends JpaRepository<Commit, Long> {
	Commit findBySha(String sha);
}
