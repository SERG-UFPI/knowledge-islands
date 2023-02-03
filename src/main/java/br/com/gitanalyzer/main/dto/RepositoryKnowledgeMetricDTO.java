package br.com.gitanalyzer.main.dto;

import javax.validation.constraints.NotNull;

import br.com.gitanalyzer.enums.KnowledgeMetric;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepositoryKnowledgeMetricDTO {

	@NotNull
	private String path;
	@NotNull
	private KnowledgeMetric knowledgeMetric;

}
