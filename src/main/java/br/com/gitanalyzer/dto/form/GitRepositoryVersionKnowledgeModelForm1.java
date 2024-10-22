package br.com.gitanalyzer.dto.form;

import java.util.List;

import br.com.gitanalyzer.model.enums.KnowledgeModel;
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

}
