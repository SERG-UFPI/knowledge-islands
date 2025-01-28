package br.com.knowledgeislands.dto.form;

import javax.validation.constraints.NotNull;

import br.com.knowledgeislands.model.enums.KnowledgeModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GitRepositoryVersionTruckFactorForm {
	@NotNull
	private String repositoryPath;
	@NotNull
	private KnowledgeModel knowledgeMetric;
}
