package br.com.gitanalyzer.model;

import java.util.List;

import br.com.gitanalyzer.enums.OperationType;
import br.com.gitanalyzer.model.entity.File;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommitFile {

	private File file;
	private OperationType operation;
	private int additions;
	private OperationType status;
	private int deletions;
	private int changes;
	private String patch;
	private List<String> addedLines;

	public CommitFile() {
		this.file = new File();
	}

	public CommitFile(File file, OperationType operation) {
		super();
		this.file = file;
		this.operation = operation;
	}

}
