package br.com.knowledgeislands.model.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.knowledgeislands.model.enums.KnowledgeModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
public class GitRepositoryVersionKnowledgeModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Enumerated(EnumType.STRING)
	private KnowledgeModel knowledgeModel;
	@JsonIgnore
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private List<ContributorVersion> contributors;
	@JsonIgnore
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private List<FileVersion> files;
	@JsonIgnore
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private List<AuthorFileExpertise> authorsFiles;
	@JsonIgnore
	@ManyToOne
	private GitRepositoryVersion repositoryVersion;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date; 
	@OneToOne
	private TruckFactor truckFactor;
	@ElementCollection
	@Column(length=500)
	private List<String> foldersPaths;
	@ManyToOne
	private GitRepositoryVersionKnowledgeModelGenAi gitRepositoryVersionKnowledgeModelGenAi;

	public GitRepositoryVersionKnowledgeModel(GitRepositoryVersion repositoryVersion, KnowledgeModel knowledgeModel, List<String> foldersPaths, GitRepositoryVersionKnowledgeModelGenAi gitRepositoryVersionKnowledgeModelGenAi) {
		this.repositoryVersion = repositoryVersion;
		this.knowledgeModel = knowledgeModel;
		this.foldersPaths = foldersPaths;
		this.date = new Date();
		this.contributors = new ArrayList<>();
		this.files = new ArrayList<>();
		this.authorsFiles = new ArrayList<>(); 
		this.gitRepositoryVersionKnowledgeModelGenAi = gitRepositoryVersionKnowledgeModelGenAi;
	}

	public GitRepositoryVersionKnowledgeModel() {
		super();
	}

}
