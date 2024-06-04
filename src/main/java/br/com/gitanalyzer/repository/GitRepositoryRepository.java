package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.GitRepository;

@Repository
public interface GitRepositoryRepository extends JpaRepository<GitRepository, Long>{

	boolean existsByName(String name);
	GitRepository findByName(String name);
	List<GitRepository> findByFilteredTrue();
	GitRepository findByFullName(String fullName);

}
