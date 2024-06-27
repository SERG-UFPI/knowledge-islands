package br.com.gitanalyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.repository.ContributorRepository;

@Service
public class ContributorSeeder {

	@Autowired
	private ContributorRepository repository;
	private static String EMAIL = "generativeai@noreply.github";
	private static String NAME = "GenerativeAi";

	public void runSeeder() {
		if(!repository.existsByEmail(EMAIL)) {
			repository.save(new Contributor(NAME, EMAIL));
		}
	}

}
