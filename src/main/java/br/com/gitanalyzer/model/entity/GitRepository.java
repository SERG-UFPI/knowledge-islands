package br.com.gitanalyzer.model.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.com.gitanalyzer.dto.ProjectDTO;
import br.com.gitanalyzer.enums.FilteredEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitRepository {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String fullName;
	private String currentPath;
	private String language;
	private boolean filtered;
	@Temporal(TemporalType.TIMESTAMP)
	private Date firstCommitDate;
	@OneToMany(mappedBy="repository", fetch=FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<GitRepositoryVersion> versions;
	@Enumerated(EnumType.STRING)
	private FilteredEnum filteredReason;
	private String defaultBranch;
	private Integer numberStars;
	private String downloadVersionHash;
	@Temporal(TemporalType.TIMESTAMP)
	private Date downloadVersionDate;
	private String cloneUrl;
	private boolean privateRepository;
	
	@javax.persistence.Transient
	private int numberAnalysedDevs, numberAllCommits, numberAllFiles;

	public GitRepository(String name, String currentPath, String fullName, String downloadVersionHash) {
		this.name = name;
		this.currentPath = currentPath;
		this.fullName = fullName;
		this.downloadVersionHash = downloadVersionHash;
	}

	public GitRepository(String name, String fullName, String language, 
			String currentPath, String defaultBranch, Integer numberStars, String downloadVersionHash) {
		super();
		this.name = name;
		this.fullName = fullName;
		this.language = language;
		this.currentPath = currentPath;
		this.defaultBranch = defaultBranch;
		this.numberStars = numberStars;
		this.downloadVersionHash = downloadVersionHash;
	}

	public ProjectDTO toDto() {
		return ProjectDTO.builder()
				.id(id)
				.currentPath(currentPath)
				.defaultBranch(defaultBranch)
				.firstCommitDate(firstCommitDate)
				.fullName(fullName)
				.mainLanguage(language)
				.name(name)
				.numberStars(numberStars)
				.build();
	}

	public GitRepository(String name, String fullName) {
		super();
		this.name = name;
		this.fullName = fullName;
	}
}		