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

	private Long id;
	private int truckfactor;
	private KnowledgeMetric knowledgeMetric;
	private ProjectVersionDTO projectVersion;
	private List<String> implicatedFiles;
	private List<TruckFactorDevelopersDTO> truckFactorDevelopers;

}
