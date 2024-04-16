package br.com.gitanalyzer.model.github_openai;

import java.util.ArrayList;
import java.util.List;

public class FileCitation implements ChatgptCitation{
	private String repositoryFullName;
	private List<SharedLink> sharedLinks;
	private FileAuthor fileAuthor;
	public FileCitation() {
		sharedLinks = new ArrayList<>();
		fileAuthor = new FileAuthor();
	}
	@Override
	public String getRepositoryFullName() {
		return repositoryFullName;
	}
	@Override
	public void setRepositoryFullName(String repositoryFullName) {
		this.repositoryFullName = repositoryFullName;
	}
	public FileAuthor getFileAuthor() {
		return fileAuthor;
	}
	public void setFileDoe(FileAuthor fileAuthor) {
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
