package br.com.gitanalyzer.model.github_openai;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommitFile {
	private String shaFile;
	private String filePath;
	private String status;
	private int additions;
	private int deletions;
	private int changes;
	private String patch;
	private List<String> addedLines;
}
