package br.com.gitanalyzer.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectDTO {

	private Long id;
	private String name;
	private String fullName;
	private String currentPath;
	private String mainLanguage;
	private Date firstCommitDate;
	private String defaultBranch;
	private Integer numberStars;

}
