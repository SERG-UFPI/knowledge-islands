package br.com.gitanalyzer.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GitRepositoryVersionDTO {

	private int numberAuthors, numberAnalysedDevs, 
	numberAllFiles, numberAnalysedFiles, numberAllCommits, numberAnalysedCommits;
	private String dateVersion; 
	private String versionId;
	private ProjectDTO project;
	private List<ContributorDTO> activeContributors;
}
