package br.com.gitanalyzer.model.github_openai;

import java.util.Date;
import java.util.List;

import br.com.gitanalyzer.model.entity.Contributor;

public class CommitCitation implements ChatgptCitation{

	private String repositoryFullName;
	private List<String> textMatchesFragments;
	private Date citationDate;
	private Contributor author;
	private Date commiterDate;
	private Contributor commiter;
	private String url;
	private String sha;
	private String nodeId;
	private String htmlUrl;
	private String commentsUrl;
	private List<FileDoe> filesDoes;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSha() {
		return sha;
	}
	public void setSha(String sha) {
		this.sha = sha;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public String getHtmlUrl() {
		return htmlUrl;
	}
	public void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}
	public String getCommentsUrl() {
		return commentsUrl;
	}
	public void setCommentsUrl(String commentsUrl) {
		this.commentsUrl = commentsUrl;
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
	public Contributor getAuthor() {
		return author;
	}
	@Override
	public void setAuthor(Contributor author) {
		this.author = author;
	}
	@Override
	public Date getCitationDate() {
		return citationDate;
	}
	@Override
	public void setCitationDate(Date citationDate) {
		this.citationDate = citationDate;
	}
	public List<FileDoe> getFilesDoes() {
		return filesDoes;
	}
	public void setFilesDoes(List<FileDoe> filesDoes) {
		this.filesDoes = filesDoes;
	}
	public Date getCommiterDate() {
		return commiterDate;
	}
	public void setCommiterDate(Date commiterDate) {
		this.commiterDate = commiterDate;
	}
	public Contributor getCommiter() {
		return commiter;
	}
	public void setCommiter(Contributor commiter) {
		this.commiter = commiter;
	}
}
