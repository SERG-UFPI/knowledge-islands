package br.com.gitanalyzer.dto.form;

import javax.validation.constraints.NotNull;

import br.com.gitanalyzer.enums.KnowledgeMetric;
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
	private String path;
	@NotNull
	private KnowledgeMetric knowledgeMetric;

}
