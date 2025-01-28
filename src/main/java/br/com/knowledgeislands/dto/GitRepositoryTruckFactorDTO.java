package br.com.knowledgeislands.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GitRepositoryTruckFactorDTO {

	private int numberAuthors, numberAnalysedDevs, numberAnalysedFiles, numberAnalysedCommits;
	private String dateVersion; 
	private String versionId;
	private GitRepositoryDTO project;
	private List<ContributorDTO> activeContributors;
}
