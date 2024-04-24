package br.com.gitanalyzer.model.github_openai;

import java.util.ArrayList;
import java.util.List;

import br.com.gitanalyzer.model.entity.SharedLink;

public class FileCitation implements ChatgptCitation{
	private String repositoryFullName;
	private List<SharedLink> sharedLinks;
	private FileLinkAuthor fileAuthor;
	public FileCitation() {
		sharedLinks = new ArrayList<>();
		fileAuthor = new FileLinkAuthor();
	}
	@Override
	public String getRepositoryFullName() {
		return repositoryFullName;
	}
	@Override
	public void setRepositoryFullName(String repositoryFullName) {
		this.repositoryFullName = repositoryFullName;
	}
	public FileLinkAuthor getFileAuthor() {
		return fileAuthor;
	}
	public void setFileDoe(FileLinkAuthor fileAuthor) {
		this.fileAuthor = fileAuthor;
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
