package br.com.knowledgeislands.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.Role;
import br.com.knowledgeislands.model.enums.RoleEnum;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{

	Role findByName(RoleEnum role);
	
	boolean existsByName(RoleEnum role);

}
