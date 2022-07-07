package br.com.gitanalyzer.model;

import java.util.Date;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Commit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	private Contributor author;
	@ManyToOne
	private Contributor commiter;
	@ManyToOne
	private Project project;
	@ElementCollection
	private List<String> parentsIds;
	private Date date;
	private String externalId;

	@Transient
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
