package br.com.knowledgeislands.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.knowledgeislands.model.entity.GitRepositoryVersionKnowledgeModelGenAi;
import br.com.knowledgeislands.repository.GitRepositoryVersionKnowledgeModelGenAiRepository;
import br.com.knowledgeislands.utils.KnowledgeIslandsUtils;

@Service
public class GitRepositoryVersionKnowledgeModelGenAiService {

	@Autowired
	private GitRepositoryVersionKnowledgeModelGenAiRepository repository;

	public List<GitRepositoryVersionKnowledgeModelGenAi> createGitRepositoryVersionKnowledgeModelGenAi(){
		List<GitRepositoryVersionKnowledgeModelGenAi> returnGenAi = new ArrayList<>();
		for (Double percentage : KnowledgeIslandsUtils.getPercentageOfGenAiFiles()) {
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
