package br.com.knowledgeislands.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

	boolean existsByEmail(String email);
	boolean existsByUsername(String username);
	User findByUsername(String username);
	Optional<User> findById(Long id);
	boolean existsById(Long id);
	
	User findByVerificationCode(String verificationCode);

}
