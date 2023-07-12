package br.com.gitanalyzer.dto.form;

import javax.validation.constraints.NotNull;

import br.com.gitanalyzer.enums.KnowledgeMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryReposTruckFactorForm {
	
	@NotNull
	private String path;
	private int numberYears;
	private int monthInterval;
	@NotNull
	private KnowledgeMetric knowledgeMetric;

}
