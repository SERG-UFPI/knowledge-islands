package br.com.knowledgeislands.model.entity;
	
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectGitHub{

	private String fullName;
	private String name;
	private String cloneUrl;
	private String default_branch;
	private String language;
	private String mainLanguage;
	private Integer stargazers_count;
}
