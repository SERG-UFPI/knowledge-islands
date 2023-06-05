package br.com.gitanalyzer.model;

import br.com.gitanalyzer.model.entity.Contributor;
import lombok.Data;

@Data
public class AuthorFile {

	private Contributor author;
	private File file;
	private boolean firstAuthor;
	private double doe;	
	private double doa;	
	private MetricsDoe metricsDoe;
	private MetricsDoa metricsDoa;

	public AuthorFile(Contributor author, File file, boolean firstAuthor) {
		super();
		this.author = author;
		this.file = file;
		this.setFirstAuthor(firstAuthor);
	}

	public AuthorFile() {
	}

	public AuthorFile(Contributor author, File file, double doe, MetricsDoe metricsDoe) {
		super();
		this.author = author;
		this.file = file;
		this.doe = doe;
		this.metricsDoe = metricsDoe;
	}

	public AuthorFile(Contributor author, double doa, File file, MetricsDoa metricsDoa) {
		super();
		this.author = author;
		this.file = file;
		this.doa = doa;
		this.metricsDoa = metricsDoa;
	}

}
