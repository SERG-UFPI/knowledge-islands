package br.com.gitanalyzer.dto.form;

import br.com.gitanalyzer.enums.KnowledgeModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GitRepositoryVersionKnowledgeModelForm3 {
	private Long idGitRepositoryVersion;
	private KnowledgeModel knowledgeMetric;
}
