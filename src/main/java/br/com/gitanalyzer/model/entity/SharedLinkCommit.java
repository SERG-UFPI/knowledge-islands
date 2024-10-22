package br.com.gitanalyzer.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
	private CommitFile commitFileAddedLink;
	@ManyToOne
	private FileRepositorySharedLinkCommit fileRepositorySharedLinkCommit;

	public SharedLinkCommit(SharedLink sharedLink, FileRepositorySharedLinkCommit fileRepositorySharedLinkCommit) {
		super();
		this.sharedLink = sharedLink;
		this.fileRepositorySharedLinkCommit = fileRepositorySharedLinkCommit;
		this.copiedLines = new ArrayList<>();
	}
}
