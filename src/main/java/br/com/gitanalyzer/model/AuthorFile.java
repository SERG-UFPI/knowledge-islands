package br.com.gitanalyzer.model;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import br.com.gitanalyzer.model.entity.ContributorVersion;
import br.com.gitanalyzer.model.entity.FileVersion;
import lombok.Data;

@Data
@Entity
public class AuthorFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@Nullable
	private ContributorVersion authorVersion;
	@OneToOne(cascade = CascadeType.PERSIST)
	private FileVersion fileVersion;
	@OneToOne
	private DOE doe;
	@OneToOne
	private DOA doa;

	public AuthorFile(ContributorVersion author, FileVersion file) {
		super();
		this.authorVersion = author;
		this.fileVersion = file;
	}

	public AuthorFile() {
		this.fileVersion = new FileVersion();
		this.authorVersion = new ContributorVersion();
	}

	public AuthorFile(ContributorVersion author, FileVersion file, DOE doe) {
		super();
		this.authorVersion = author;
		this.fileVersion = file;
		this.doe = doe;
	}

	public AuthorFile(ContributorVersion author, FileVersion file, DOA doa) {
		super();
		this.authorVersion = author;
		this.fileVersion = file;
		this.doa = doa;
		this.doa = doa;
	}

}
