package br.com.knowledgeislands.model.entity;

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
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private DOE doe;
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private DOA doa;
	private boolean genAiEffect;

	public AuthorFileExpertise(ContributorVersion contributorVersion, FileVersion file) {
		super();
		this.contributorVersion = contributorVersion;
		this.fileVersion = file;
	}

	public AuthorFileExpertise() {
		this.fileVersion = new FileVersion();
		this.contributorVersion = new ContributorVersion();
	}

	public AuthorFileExpertise(ContributorVersion contributorVersion, FileVersion file, DOE doe, boolean genAiEffect) {
		super();
		this.contributorVersion = contributorVersion;
		this.fileVersion = file;
		this.doe = doe;
		this.genAiEffect = genAiEffect;
	}

	public AuthorFileExpertise(ContributorVersion contributorVersion, FileVersion file, DOA doa, boolean genAiEffect) {
		super();
		this.contributorVersion = contributorVersion;
		this.fileVersion = file;
		this.doa = doa;
		this.genAiEffect = genAiEffect;
	}

}
