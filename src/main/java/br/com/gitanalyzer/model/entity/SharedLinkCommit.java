package br.com.gitanalyzer.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class SharedLinkCommit {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToMany(cascade= {CascadeType.PERSIST, CascadeType.REMOVE})
	private List<CodeLine> copiedLines;
	@OneToOne
	private SharedLink sharedLink;
	@OneToOne(cascade = {CascadeType.PERSIST})
	private CommitFile commitFileThatAddedTheLink;

	public SharedLinkCommit(SharedLink sharedLink) {
		super();
		this.sharedLink = sharedLink;
		this.copiedLines = new ArrayList<>();
	}
}
