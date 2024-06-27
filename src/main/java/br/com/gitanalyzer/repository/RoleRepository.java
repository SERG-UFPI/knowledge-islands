package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.enums.RoleEnum;
import br.com.gitanalyzer.model.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{

	Role findByName(RoleEnum role);
	
	boolean existsByName(RoleEnum role);

}
