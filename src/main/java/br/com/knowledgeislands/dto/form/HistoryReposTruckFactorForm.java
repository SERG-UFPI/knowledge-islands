package br.com.knowledgeislands.dto.form;

import javax.validation.constraints.NotNull;

import br.com.knowledgeislands.model.enums.KnowledgeModel;
import br.com.knowledgeislands.model.enums.TimeIntervalTypeEnum;
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
	private int interval;
	@NotNull
	private TimeIntervalTypeEnum intervalType;
	@NotNull
	private KnowledgeModel knowledgeMetric;

}
