package br.com.gitanalyzer.model.github_openai;

import java.util.Date;
import java.util.List;

import br.com.gitanalyzer.model.entity.Contributor;

public class CommitCitation implements ChatgptCitation{

	private String repositoryFullName;
	private List<SharedLink> sharedLinks;
	private Date commiterDate;
	private Contributor commiter;
	private String url;
	private String sha;
	private String nodeId;
	private String htmlUrl;
	private String commentsUrl;
	private List<FileAuthor> filesAuthor;
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
	public List<FileAuthor> getFilesAuthor() {
		return filesAuthor;
	}
	public void setFilesAuthor(List<FileAuthor> filesAuthor) {
		this.filesAuthor = filesAuthor;
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
	@Override
	public List<SharedLink> getSharedLinks() {
		return sharedLinks;
	}
	@Override
	public void setSharedLinks(List<SharedLink> sharedLinks) {
		this.sharedLinks = sharedLinks;
	}
}
