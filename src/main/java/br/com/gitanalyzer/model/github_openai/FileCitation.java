package br.com.gitanalyzer.model.github_openai;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.gitanalyzer.model.entity.Contributor;

public class FileCitation implements ChatgptCitation{
	
	private String repositoryFullName;
	private List<String> textMatchesFragments;
	private FileDoe fileDoe;
	private Contributor authorCitation;
	private Date citationDate;
	public FileCitation() {
		fileDoe = new FileDoe();
		textMatchesFragments = new ArrayList<>();
		authorCitation = new Contributor();
	}
	@Override
	public String getRepositoryFullName() {
		return repositoryFullName;
	}
	@Override
	public void setRepositoryFullName(String repositoryFullName) {
		this.repositoryFullName = repositoryFullName;
	}
	@Override
	public List<String> getTextMatchesFragments() {
		return textMatchesFragments;
	}
	@Override
	public void setTextMatchesFragments(List<String> textMatchesFragments) {
		this.textMatchesFragments = textMatchesFragments;
	}
	@Override
	public void setAuthorCitation(Contributor authorCitation) {
		this.authorCitation = authorCitation;
	}
	@Override
	public Contributor getAuthorCitation() {
		return authorCitation;
	}
	@Override
	public Date getCitationDate() {
		return citationDate;
	}
	@Override
	public void setCitationDate(Date citationDate) {
		this.citationDate = citationDate;
	}
	public FileDoe getFileDoe() {
		return fileDoe;
	}
	public void setFileDoe(FileDoe fileDoe) {
		this.fileDoe = fileDoe;
	}

}
