package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

	boolean existsByEmail(String email);
	boolean existsByUsername(String username);
	User findByUsername(String username);
	boolean existsById(Long id);
	
}
