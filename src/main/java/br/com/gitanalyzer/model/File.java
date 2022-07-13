package br.com.gitanalyzer.model;

import lombok.Data;

@Data
public class File {

	private String path;
	private Project project;
	private String extension;
	private int fileSize;

	public File(String path, Project project, String extension) {
		super();
		this.path = path;
		this.project = project;
		this.extension = extension;
	}

	public File(String path, Project project, String extension, int fileSize) {
		super();
		this.path = path;
		this.project = project;
		this.extension = extension;
		this.fileSize = fileSize;
	}

	public File(String path) {
		super();
		this.path = path;
	}

	public File() {
	}

}
