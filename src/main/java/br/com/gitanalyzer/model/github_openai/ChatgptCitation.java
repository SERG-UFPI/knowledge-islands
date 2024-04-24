package br.com.gitanalyzer.model.github_openai;

import java.util.List;

import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.SharedLink;


public interface ChatgptCitation {
	public String getRepositoryFullName();
	public void setRepositoryFullName(String repositoryFullName);
	public List<SharedLink> getSharedLinks();
	public void setSharedLinks(List<SharedLink> sharedLinks);
}
