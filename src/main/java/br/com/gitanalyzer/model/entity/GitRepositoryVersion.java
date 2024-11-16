package br.com.gitanalyzer.model.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GitRepositoryVersion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private int numberAnalysedDevs; 
	private int numberAnalysedFiles;
	private int numberAnalysedCommits;
	private double timeToExtract;
	private String versionId;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateVersion; 
	@ManyToOne(optional = false)
	private GitRepository gitRepository;
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private List<Contributor> contributors;
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private List<File> files;
	@JsonIgnore
	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "repositoryVersion")
	private List<GitRepositoryVersionKnowledgeModel> gitRepositoryVersionKnowledgeModel;
	@JsonIgnore
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private List<Commit> commits;
	@JsonIgnore
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private List<GitRepositoryDependency> dependencies;
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private GitRepositoryFolder rootFolder;
	@JsonIgnore
	@OneToMany(mappedBy="gitRepositoryVersion", cascade = CascadeType.REMOVE)
	private List<GitRepositoryVersionProcess> processes;

	public GitRepositoryVersion(GitRepository gitRepository, int numberAnalysedDevs, int numberAnalysedFiles, 
			int numberAnalysedCommits, Date dateVersion, String versionId, List<Contributor> contributors, 
			List<Commit> commits, List<File> files, Double timeToExtract, GitRepositoryFolder rootFolder) {
		super();
		this.gitRepository = gitRepository;
		this.numberAnalysedDevs = numberAnalysedDevs;
		this.numberAnalysedFiles = numberAnalysedFiles;
		this.numberAnalysedCommits = numberAnalysedCommits;
		this.dateVersion = dateVersion;
		this.versionId = versionId;
		this.contributors = contributors;
		this.commits = commits;
		this.files = files;
		this.timeToExtract = timeToExtract;
		this.rootFolder = rootFolder;
		gitRepositoryVersionKnowledgeModel = new ArrayList<>();
	}

	public String getRepositoryLanguage() {
		return gitRepository.getLanguage();
	}

	public boolean validGitRepositoryVersion() {
		return this.getContributors() != null && this.getCommits() != null && 
				this.getFiles() != null && !this.getContributors().isEmpty() && 
				!this.getCommits().isEmpty() && !this.getFiles().isEmpty();
	}

}
