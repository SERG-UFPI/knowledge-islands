package br.com.gitanalyzer.main.dto;

import javax.validation.constraints.NotNull;

import br.com.gitanalyzer.enums.KnowledgeMetric;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PathKnowledgeMetricDTO {

	@NotNull
	private String path;
	@NotNull
	private KnowledgeMetric knowledgeMetric;

}
