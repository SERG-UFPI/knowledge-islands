package br.com.gitanalyzer.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class FileGitRepositorySharedLinkCommit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToOne
	private File file;
	@OneToOne
	private GitRepository gitRepository;
	@ManyToMany(cascade = {CascadeType.PERSIST})
	private List<SharedLinkCommit> sharedLinks;

	public FileGitRepositorySharedLinkCommit(File file, GitRepository gitRepository) {
		this.file = file;
		this.gitRepository = gitRepository;
		sharedLinks = new ArrayList<>();
	}

}
