package br.com.gitanalyzer.model.github_openai;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class File {
	private int size;
	private String name;
	private String path;
	private String sha;
	private String url;
	private String htmlUrl;
	private String downloadUrl;
	private String gitUrl;
	private String contentEncoded;
	private String contentDecoded;
	private String encoding;
	private List<Commit> commits;
	
	public File() {
		commits = new ArrayList<>();
	}
}
