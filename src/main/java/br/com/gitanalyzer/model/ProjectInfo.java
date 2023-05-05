package br.com.gitanalyzer.model;
	
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInfo{

	private String fullName;
	private String name;
	private String cloneUrl;
	private String default_branch;
	private String language;
	private String mainLanguage;
	private Integer stargazers_count;
}
