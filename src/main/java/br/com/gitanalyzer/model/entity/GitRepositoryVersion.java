package br.com.gitanalyzer.model.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
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

import br.com.gitanalyzer.model.Commit;
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
	private int numberAnalysedDevs, numberAnalysedFiles, numberAnalysedCommits;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateVersion; 
	private String versionId;
	@ManyToOne(optional = false)
	private GitRepository gitRepository;
	@OneToMany(cascade = CascadeType.ALL)
	private List<Contributor> contributors;
	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL)
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
	private double timeToExtract;
	@JsonIgnore
	@OneToMany(mappedBy="gitRepositoryVersion", cascade = CascadeType.REMOVE)
	private List<GitRepositoryVersionProcess> processes;

	public GitRepositoryVersion(int numberAnalysedDevs, int numberAnalysedFiles, 
			int numberAnalysedCommits, Date dateVersion, String versionId, List<Contributor> contributors, 
			List<Commit> commits, List<File> files, Double timeToExtract, List<GitRepositoryDependency> dependencies, GitRepositoryFolder rootFolder) {
		super();
		this.numberAnalysedDevs = numberAnalysedDevs;
		this.numberAnalysedFiles = numberAnalysedFiles;
		this.numberAnalysedCommits = numberAnalysedCommits;
		this.dateVersion = dateVersion;
		this.versionId = versionId;
		this.contributors = contributors;
		this.commits = commits;
		this.files = files;
		this.timeToExtract = timeToExtract;
		this.dependencies = dependencies;
		this.rootFolder = rootFolder;
		gitRepositoryVersionKnowledgeModel = new ArrayList<>();
	}

	public String getRepositoryLanguage() {
		return gitRepository.getLanguage();
	}

	//	public GitRepositoryTruckFactorDTO toDto() {
	//		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	//		return GitRepositoryTruckFactorDTO.builder()
	//				.project(gitRepository.toDto())
	//				.activeContributors(contributors!=null?contributors.stream().filter(c -> c.isActive()).map(c -> c.toDto()).toList():null)
	//				.dateVersion(dateVersion!=null?fmt.format(dateVersion):null)
	//				.numberAnalysedCommits(numberAnalysedCommits)
	//				.numberAnalysedDevs(numberAnalysedDevs)
	//				.numberAnalysedFiles(numberAnalysedFiles)
	//				.versionId(versionId)
	//				.build();
	//	}

	public boolean validTruckFactor() {
		return this.getContributors() != null && this.getCommits() != null && 
				this.getFiles() != null && this.getContributors().size() > 0 && 
				this.getCommits().size() > 0 && this.getFiles().size() > 0;
	}

}
