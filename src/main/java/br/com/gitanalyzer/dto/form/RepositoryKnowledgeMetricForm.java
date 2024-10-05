package br.com.gitanalyzer.dto.form;

import java.util.List;

import javax.validation.constraints.NotNull;

import br.com.gitanalyzer.model.enums.KnowledgeModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepositoryKnowledgeMetricForm {

	@NotNull
	private String repositoryPath;
	@NotNull
	private KnowledgeModel knowledgeMetric;
	private List<String> foldersPaths;

	public RepositoryKnowledgeMetricForm(@NotNull String repositoryPath, @NotNull KnowledgeModel knowledgeMetric) {
		super();
		this.repositoryPath = repositoryPath;
		this.knowledgeMetric = knowledgeMetric;
	}

}
