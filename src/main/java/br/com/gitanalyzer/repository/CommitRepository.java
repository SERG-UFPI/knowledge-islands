package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.Commit;

@Repository
public interface CommitRepository extends JpaRepository<Commit, Long> {
	Commit findBySha(String sha);
}
