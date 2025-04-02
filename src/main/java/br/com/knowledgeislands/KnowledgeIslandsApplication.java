
package br.com.knowledgeislands;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import br.com.knowledgeislands.service.CleanProjectService;
import br.com.knowledgeislands.service.ContributorSeeder;
import br.com.knowledgeislands.service.RoleSeeder;

@SpringBootApplication
@EnableScheduling
public class KnowledgeIslandsApplication {

	@Autowired
	private RoleSeeder roleSeeder;
	@Autowired
	private ContributorSeeder contributorSeeder;
	@Autowired
	private CleanProjectService cleanProjectService;

	public static void main(String[] args) {
		SpringApplication.run(KnowledgeIslandsApplication.class, args);
	}

	@PostConstruct
	public void init() {
		roleSeeder.runSeeder();
		contributorSeeder.runSeeder();
		cleanProjectService.cleanDowloadedProject();
	}

}
