package br.com.knowledgeislands.dto;

import java.util.List;

import br.com.knowledgeislands.model.enums.KnowledgeModel;
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
	private KnowledgeModel knowledgeMetric;
	private GitRepositoryTruckFactorDTO projectVersion;
	private List<String> implicatedFiles;
	private List<ContributorDTO> truckFactorDevelopers;

}
