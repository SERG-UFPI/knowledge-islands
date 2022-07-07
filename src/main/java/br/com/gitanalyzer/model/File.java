package br.com.gitanalyzer.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class File {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String path;
	@ManyToOne
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
