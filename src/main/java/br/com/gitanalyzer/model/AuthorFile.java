package br.com.gitanalyzer.model;

import lombok.Data;

@Data
public class AuthorFile {

	private Contributor author;
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
