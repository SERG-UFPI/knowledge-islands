package br.com.gitanalyzer.model.github_openai;

import java.util.Date;
import java.util.List;

import br.com.gitanalyzer.model.entity.Contributor;


public interface ChatgptCitation {
	public String getRepositoryFullName();
	public void setRepositoryFullName(String repositoryFullName);
	public List<String> getTextMatchesFragments();
	public void setTextMatchesFragments(List<String> textMatchesFragments);
	public void setAuthorCitation(Contributor authorCitation);
	public Contributor getAuthorCitation();
	public Date getCitationDate();
	public void setCitationDate(Date citationDate);
}
