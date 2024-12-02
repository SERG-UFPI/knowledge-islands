package br.com.gitanalyzer.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.model.entity.GitRepositoryVersionKnowledgeModelGenAi;
import br.com.gitanalyzer.repository.GitRepositoryVersionKnowledgeModelGenAiRepository;
import br.com.gitanalyzer.utils.KnowledgeIslandsUtils;

@Service
public class GitRepositoryVersionKnowledgeModelGenAiService {

	@Autowired
	private GitRepositoryVersionKnowledgeModelGenAiRepository repository;

	public List<GitRepositoryVersionKnowledgeModelGenAi> createGitRepositoryVersionKnowledgeModelGenAi(){
		List<GitRepositoryVersionKnowledgeModelGenAi> returnGenAi = new ArrayList<>();
		List<Double> percentages = KnowledgeIslandsUtils.getPercentageOfGenAiFiles();
		for (Double percentage : percentages) {
			GitRepositoryVersionKnowledgeModelGenAi modelGenAi = repository.findByAvgPctFilesGenAi(percentage);
			if(modelGenAi == null) {
				returnGenAi.add(repository.save(GitRepositoryVersionKnowledgeModelGenAi.builder().avgPctFilesGenAi(percentage).build()));
			}else {
				returnGenAi.add(modelGenAi);
			}
		}
		return returnGenAi;
	}
}
