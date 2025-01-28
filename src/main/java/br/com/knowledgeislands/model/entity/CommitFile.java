package br.com.knowledgeislands.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import br.com.knowledgeislands.model.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class CommitFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private int additions;
	private int additionsCodes;
	@Enumerated(EnumType.STRING)
	private OperationType status;
	private int deletions;
	private int changes;
	@Lob
	@Column(columnDefinition="TEXT")
	private String patch;
	@OneToMany(mappedBy="commitFile", cascade= CascadeType.REMOVE)
	private List<CodeLine> addedCodeLines;
	@ManyToOne
	private File file;
	@ManyToOne
	private Commit commit;
	@OneToOne
	private CommitFileGenAi commitFileGenAi;

	public CommitFile(File file, OperationType status, Commit commit) {
		super();
		this.file = file;
		this.commit = commit;
		this.status = status;
		this.addedCodeLines = new ArrayList<>();
	}

}
