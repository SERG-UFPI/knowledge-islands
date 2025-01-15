package br.com.gitanalyzer.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class CodeLine {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(length=1000, nullable = false)
	private String line;
	@ManyToOne
	private SharedLinkCommit sharedLinkCommit;
	@ManyToOne
	private CommitFile commitFile;

	public CodeLine(String line) {
		this.line = line;
	}
}
