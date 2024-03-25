package br.com.gitanalyzer.model.github_openai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileContent {
	private String name;
	private String path;
	private String sha;
	private int size;
	private String url;
	private String htmlUrl;
	private String gitUrl;
	private String downloadUrl;
	private String contentEncoded;
	private String contentDecoded;
}
