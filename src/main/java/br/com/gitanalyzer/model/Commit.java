package br.com.gitanalyzer.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.gitanalyzer.model.entity.Contributor;
import lombok.Data;

@Data
public class Commit {

	private Contributor author;
	//private Project project;
	private Date date;
	private String externalId;
	private int numberOfFilesTouched;

	private List<CommitFile> commitFiles = new ArrayList<CommitFile>();

	public Commit(Contributor author, Date date, String externalId) {
		super();
		this.author = author;
		//this.project = project;
		this.date = date;
		this.externalId = externalId;
	}

	public Commit(Date date, String externalId) {
		super();
		this.date = date;
		this.externalId = externalId;
	}

	public Commit() {
	}

}
