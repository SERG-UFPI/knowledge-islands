package br.com.gitanalyzer.model;

import br.com.gitanalyzer.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommitFile {

	private File file;
	private OperationType operation;
	private int adds;

	public CommitFile() {
	}

	public CommitFile(File file, OperationType operation) {
		super();
		this.file = file;
		this.operation = operation;
	}

}
