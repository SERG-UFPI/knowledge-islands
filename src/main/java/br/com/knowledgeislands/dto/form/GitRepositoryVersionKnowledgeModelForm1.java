package br.com.knowledgeislands.dto.form;

import java.util.List;

import br.com.knowledgeislands.model.entity.GitRepositoryVersionKnowledgeModelGenAi;
import br.com.knowledgeislands.model.enums.KnowledgeModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GitRepositoryVersionKnowledgeModelForm1 {

	private Long idGitRepositoryVersion;
	private KnowledgeModel knowledgeMetric;
	private List<String> foldersPaths;
	private GitRepositoryVersionKnowledgeModelGenAi modelGenAi;

}
