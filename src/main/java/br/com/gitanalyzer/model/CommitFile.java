package br.com.gitanalyzer.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import br.com.gitanalyzer.enums.OperationType;
import lombok.Data;

@Entity
@Data
public class CommitFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	private File file;
	@ManyToOne
	private Commit commit;
	@Enumerated(EnumType.STRING)
	private OperationType operation;
	private int adds;

	public CommitFile(File file, OperationType operation) {
		super();
		this.file = file;
		this.operation = operation;
	}

	public CommitFile() {
	}

}
