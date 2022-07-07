package br.com.gitanalyzer.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AuthorFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToOne
	private Contributor author;
	@OneToOne
	private File file;
	private boolean firstAuthor;
	private double doe;	
	private double doa;	

	public AuthorFile(Contributor author, File file, boolean firstAuthor) {
		super();
		this.author = author;
		this.file = file;
		this.setFirstAuthor(firstAuthor);
	}

	public AuthorFile() {
	}

	public AuthorFile(Contributor author, File file, double doe) {
		super();
		this.author = author;
		this.file = file;
		this.doe = doe;
	}

	public AuthorFile(Contributor author, double doa, File file) {
		super();
		this.author = author;
		this.file = file;
		this.doa = doa;
	}

}
