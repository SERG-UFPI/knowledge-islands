package br.com.gitanalyzer.model;

import br.com.gitanalyzer.model.entity.Contributor;
import br.com.gitanalyzer.model.entity.File;
import lombok.Data;

@Data
public class AuthorFile {

	private Contributor author;
	private File file;
	private boolean firstAuthor;
	private DOE doe;
	private DOA doa;

	public AuthorFile(Contributor author, File file, boolean firstAuthor) {
		super();
		this.author = author;
		this.file = file;
		this.setFirstAuthor(firstAuthor);
	}

	public AuthorFile() {
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
