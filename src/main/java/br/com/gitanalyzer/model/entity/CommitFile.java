package br.com.gitanalyzer.model.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import br.com.gitanalyzer.model.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Entity
@AllArgsConstructor
public class CommitFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	private File file;
	private int additions;
	@Enumerated(EnumType.STRING)
	private OperationType status;
	private int deletions;
	private int changes;
	@Lob
	@Column(columnDefinition="TEXT")
	private String patch;
	@ElementCollection
	private List<String> addedLines;

	public CommitFile() {
		this.file = new File();
	}

	public CommitFile(File file, OperationType status) {
		super();
		this.file = file;
		this.status = status;
	}
	
	public CommitFile(File file, OperationType status, List<String> addedLines) {
		super();
		this.file = file;
		this.status = status;
		this.addedLines = addedLines;
	}

}
