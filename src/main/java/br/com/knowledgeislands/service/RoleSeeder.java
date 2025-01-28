package br.com.knowledgeislands.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.knowledgeislands.model.entity.Role;
import br.com.knowledgeislands.model.enums.RoleEnum;
import br.com.knowledgeislands.repository.RoleRepository;

@Service
public class RoleSeeder {

	@Autowired
	private RoleRepository repository;
	
	public void runSeeder() {
		if(repository.existsByName(RoleEnum.ROLE_ADMIN) == false) {
			repository.save(Role.builder().name(RoleEnum.ROLE_ADMIN).build());
		}
		if(repository.existsByName(RoleEnum.ROLE_USER) == false) {
			repository.save(Role.builder().name(RoleEnum.ROLE_USER).build());
		}
	}
	
}
