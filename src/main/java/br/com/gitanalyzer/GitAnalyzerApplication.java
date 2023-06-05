
package br.com.gitanalyzer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.com.gitanalyzer.service.RoleSeeder;

@SpringBootApplication
public class GitAnalyzerApplication {

	@Autowired
	private RoleSeeder roleSeeder;

	public static void main(String[] args) {
		SpringApplication.run(GitAnalyzerApplication.class, args);
	}

	@PostConstruct
	public void init() {
		roleSeeder.runSeeder();
	}

}
