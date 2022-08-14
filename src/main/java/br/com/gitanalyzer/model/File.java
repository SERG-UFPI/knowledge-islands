package br.com.gitanalyzer.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class File {

	private String path;
	private Project project;
	private String extension;
	private int fileSize;
	private Set<Contributor> mantainers = new HashSet<Contributor>();
	private List<String> renamePaths = new ArrayList<String>();

	public boolean isFile(String path) {
		List<String> paths = new ArrayList<String>();
		paths.add(this.path);
		paths.addAll(renamePaths);
		return paths.contains(path);
	}

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
