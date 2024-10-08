package br.com.gitanalyzer.model.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import lombok.Data;

@Data
@Entity
public class AuthorFileExpertise {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	private ContributorVersion authorVersion;
	@OneToOne
	private FileVersion fileVersion;
	@OneToOne(cascade = CascadeType.PERSIST)
	private DOE doe;
	@OneToOne(cascade = CascadeType.PERSIST)
	private DOA doa;
	@Transient
	private GitRepositoryVersionKnowledgeModel gitRepositoryVersionKnowledgeModel;
	private boolean withGenAi;

	public AuthorFileExpertise(ContributorVersion author, FileVersion file) {
		super();
		this.authorVersion = author;
		this.fileVersion = file;
		this.withGenAi = false;
	}

	public AuthorFileExpertise() {
		this.fileVersion = new FileVersion();
		this.authorVersion = new ContributorVersion();
		this.withGenAi = false;
	}

	public AuthorFileExpertise(ContributorVersion author, FileVersion file, DOE doe) {
		super();
		this.authorVersion = author;
		this.fileVersion = file;
		this.doe = doe;
		this.withGenAi = false;
	}

	public AuthorFileExpertise(ContributorVersion author, FileVersion file, DOA doa) {
		super();
		this.authorVersion = author;
		this.fileVersion = file;
		this.doa = doa;
		this.withGenAi = false;
	}

}
