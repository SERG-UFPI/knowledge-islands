package br.com.gitanalyzer.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.File;
import lombok.Data;

@Data
@Entity
public class AuthorFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	private Contributor author;
	@OneToOne(cascade = CascadeType.PERSIST)
	private File file;
	@OneToOne
	private DOE doe;
	@OneToOne
	private DOA doa;

	public AuthorFile(Contributor author, File file) {
		super();
		this.author = author;
		this.file = file;
	}

	public AuthorFile() {
		this.file = new File();
		this.author = new Contributor();
	}

	public AuthorFile(Contributor author, File file, DOE doe) {
		super();
		this.author = author;
		this.file = file;
		this.doe = doe;
	}

	public AuthorFile(Contributor author, File file, DOA doa) {
		super();
		this.author = author;
		this.file = file;
		this.doa = doa;
		this.doa = doa;
	}

}
