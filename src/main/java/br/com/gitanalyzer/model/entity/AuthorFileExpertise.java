package br.com.gitanalyzer.model.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import lombok.Data;

@Data
@Entity
public class AuthorFileExpertise {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	private ContributorVersion contributorVersion;
	@OneToOne
	private FileVersion fileVersion;
	@OneToOne(cascade = CascadeType.PERSIST)
	private DOE doe;
	@OneToOne(cascade = CascadeType.PERSIST)
	private DOA doa;

	public AuthorFileExpertise(ContributorVersion contributorVersion, FileVersion file) {
		super();
		this.contributorVersion = contributorVersion;
		this.fileVersion = file;
	}

	public AuthorFileExpertise() {
		this.fileVersion = new FileVersion();
		this.contributorVersion = new ContributorVersion();
	}

	public AuthorFileExpertise(ContributorVersion contributorVersion, FileVersion file, DOE doe) {
		super();
		this.contributorVersion = contributorVersion;
		this.fileVersion = file;
		this.doe = doe;
	}

	public AuthorFileExpertise(ContributorVersion contributorVersion, FileVersion file, DOA doa) {
		super();
		this.contributorVersion = contributorVersion;
		this.fileVersion = file;
		this.doa = doa;
	}

}
