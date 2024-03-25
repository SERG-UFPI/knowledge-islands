package br.com.gitanalyzer.model.github_openai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommitFile {
	private String sha;
	private String fileName;
	private String status;
	private int additions;
	private int deletions;
	private int changes;
	private String patch;
}
