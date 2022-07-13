package br.com.gitanalyzer.model;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class Commit {

	private Long id;
	private Contributor author;
	private Contributor commiter;
	private Project project;
	private List<String> parentsIds;
	private Date date;
	private String externalId;

	private List<CommitFile> commitFiles;

	public Commit(Contributor author, Contributor commiter, Project project, Date date, String externalId, List<String> parentsIds) {
		super();
		this.author = author;
		this.commiter = commiter;
		this.project = project;
		this.date = date;
		this.externalId = externalId;
		this.parentsIds = parentsIds;
	}

	public Commit() {
	}

}
