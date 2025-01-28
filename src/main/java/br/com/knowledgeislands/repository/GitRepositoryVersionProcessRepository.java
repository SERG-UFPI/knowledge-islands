package br.com.knowledgeislands.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.GitRepositoryVersionProcess;

@Repository
public interface GitRepositoryVersionProcessRepository extends JpaRepository<GitRepositoryVersionProcess, Long>{

	List<GitRepositoryVersionProcess> findByUserId(Long id);

}
