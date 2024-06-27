package br.com.gitanalyzer.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import br.com.gitanalyzer.model.entity.ContributorVersion;
import br.com.gitanalyzer.model.entity.FileVersion;
import br.com.gitanalyzer.model.entity.GitRepositoryVersionKnowledgeModel;
import lombok.Data;

@Data
@Entity
public class AuthorFile {

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

	public AuthorFile(ContributorVersion author, FileVersion file) {
		super();
		this.authorVersion = author;
		this.fileVersion = file;
		this.withGenAi = false;
	}

	public AuthorFile() {
		this.fileVersion = new FileVersion();
		this.authorVersion = new ContributorVersion();
		this.withGenAi = false;
	}

	public AuthorFile(ContributorVersion author, FileVersion file, DOE doe) {
		super();
		this.authorVersion = author;
		this.fileVersion = file;
		this.doe = doe;
		this.withGenAi = false;
	}

	public AuthorFile(ContributorVersion author, FileVersion file, DOA doa) {
		super();
		this.authorVersion = author;
		this.fileVersion = file;
		this.doa = doa;
		this.doa = doa;
		this.withGenAi = false;
	}

}
