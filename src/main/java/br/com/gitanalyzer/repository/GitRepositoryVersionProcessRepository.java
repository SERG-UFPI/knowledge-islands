package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.GitRepositoryVersionProcess;

@Repository
public interface GitRepositoryVersionProcessRepository extends JpaRepository<GitRepositoryVersionProcess, Long>{

	List<GitRepositoryVersionProcess> findByUserId(Long id);

}
