package br.com.gitanalyzer.dto;

import java.util.List;

import br.com.gitanalyzer.enums.KnowledgeMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TruckFactorDTO {

	private int truckfactor;
	private KnowledgeMetric knowledgeMetric;
	private List<String> implicatedFiles;

}
