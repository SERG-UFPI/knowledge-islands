package br.com.knowledgeislands.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GitRepositoryVersionProcessDTO {
	private Long id;
	private String startDate;
	private String endDate;
	private String stage;
	private String repositoryUrl;
	private Long idGitRepositoryVersion;
}
