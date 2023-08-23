package br.com.gitanalyzer.model.entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.com.gitanalyzer.dto.ProjectVersionDTO;
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
public class ProjectVersion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private int numberAllDevs, numberAuthors, numberAnalysedDevs, 
	numberAllFiles, numberAnalysedFiles, numberAllCommits, numberAnalysedCommits;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateVersion; 
	private String versionId;
	@ManyToOne(optional = false)
	private Project project;
	@OneToMany(cascade = CascadeType.ALL)
	private List<Contributor> contributors;
	@OneToMany(cascade = CascadeType.ALL)
	private List<File> files;
	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "projectVersion")
	private List<TruckFactor> truckFactors;

	@javax.persistence.Transient
	private List<Commit> commits;

	public ProjectVersion(int numberAllDevs, int numberAnalysedDevs, int numberAllFiles,
			int numberAnalysedFiles, int numberAllCommits, int numberAnalysedCommits, Date dateVersion,
			String versionId, List<Contributor> contributors) {
		super();
		this.numberAllDevs = numberAllDevs;
		this.numberAnalysedDevs = numberAnalysedDevs;
		this.numberAllFiles = numberAllFiles;
		this.numberAnalysedFiles = numberAnalysedFiles;
		this.numberAllCommits = numberAllCommits;
		this.numberAnalysedCommits = numberAnalysedCommits;
		this.dateVersion = dateVersion;
		this.versionId = versionId;
		this.contributors = contributors;
	}

	public String getProjectLanguage() {
		return project.getMainLanguage();
	}

	public ProjectVersionDTO toDto() {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return ProjectVersionDTO.builder()
				.project(project.toDto())
				.activeContributors(contributors.stream().filter(c -> c.isActive()).map(c -> c.toDto()).toList())
				.dateVersion(dateVersion!=null?fmt.format(dateVersion):null)
				.numberAllCommits(numberAllCommits)
				.numberAllDevs(numberAllDevs)
				.numberAllFiles(numberAllFiles)
				.numberAnalysedCommits(numberAnalysedCommits)
				.numberAnalysedDevs(numberAnalysedDevs)
				.numberAnalysedFiles(numberAnalysedFiles)
				.numberAuthors(numberAuthors)
				.versionId(versionId)
				.build();
	}

}
