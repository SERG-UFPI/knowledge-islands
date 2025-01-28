package br.com.knowledgeislands.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TruckFactorAnalysisDTO {

	private TruckFactorDTO truckFactor;
	private GitRepositoryTruckFactorDTO projectVersion;
	private GitRepositoryDTO project;
}
