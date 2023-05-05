package br.com.gitanalyzer.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectVersion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private int numberAllDevs, numberAuthors, numberAnalysedDevs, 
	numberAllFiles, numberAnalysedFiles, numberAllCommits, numberAnalysedCommits;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateVersion; 
	private String versionId;
	@ManyToOne
	private Project project;

	@javax.persistence.Transient
	private List<Commit> commits;
	@javax.persistence.Transient
	private List<Contributor> contributors;
	@javax.persistence.Transient
	private List<File> files;
	@javax.persistence.Transient
	private String projectName;
	@javax.persistence.Transient
	private Date firstCommitDate;

	public ProjectVersion(int numberAllDevs, int numberAnalysedDevs, int numberAllFiles,
			int numberAnalysedFiles, int numberAllCommits, int numberAnalysedCommits, Date dateVersion,
			String versionId) {
		super();
		this.numberAllDevs = numberAllDevs;
		this.numberAnalysedDevs = numberAnalysedDevs;
		this.numberAllFiles = numberAllFiles;
		this.numberAnalysedFiles = numberAnalysedFiles;
		this.numberAllCommits = numberAllCommits;
		this.numberAnalysedCommits = numberAnalysedCommits;
		this.dateVersion = dateVersion;
		this.versionId = versionId;
	}

	public String getProjectLanguage() {
		return project.getMainLanguage();
	}

}
