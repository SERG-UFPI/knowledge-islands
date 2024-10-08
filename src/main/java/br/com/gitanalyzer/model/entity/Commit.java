package br.com.gitanalyzer.model.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import lombok.Data;

@Data
@Entity
public class Commit implements Comparable<Commit>{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String sha;
	private String nodeId;
	private String url;
	private String htmlUrl;
	private String commentsUrl;
	@Temporal(TemporalType.TIMESTAMP)
	private Date authorDate;
	@ManyToOne
	private Contributor author;
	@Temporal(TemporalType.TIMESTAMP)
	private Date committerDate;
	@Transient
	private Contributor committer;
	@Column(length=1000)
	private String message;
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private List<CommitFile> commitFiles = new ArrayList<>();

	public Commit(Contributor author, Date date, String externalId, String message) {
		super();
		this.message = message;
		this.author = author;
		this.authorDate = date;
		this.sha = externalId;
	}

	public Commit(Date date, String externalId) {
		super();
		this.authorDate = date;
		this.sha = externalId;
	}

	public Commit() {
	}

	@Override
	public int compareTo(Commit other) {
		return this.authorDate.compareTo(other.getAuthorDate());
	}

}
