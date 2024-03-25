package br.com.gitanalyzer.model.github_openai;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.gitanalyzer.model.entity.Contributor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Commit {
	private String sha;
	private String nodeId;
	private String url;
	private String htmlUrl;
	private String commentsUrl;
	private Date authorDate;
	private Contributor author;
	private Date committerDate;
	private Contributor committer;
	private String message;
	private List<CommitFile> commitFiles;
	
	public Commit() {
		commitFiles = new ArrayList<>();
	}
}
