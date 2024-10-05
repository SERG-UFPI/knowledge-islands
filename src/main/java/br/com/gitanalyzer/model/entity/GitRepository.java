package br.com.gitanalyzer.model.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.gitanalyzer.dto.GitRepositoryDTO;
import br.com.gitanalyzer.model.enums.FilteredEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitRepository {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	@Column(unique=true)
	private String fullName;
	private String currentFolderPath;
	private String language;
	private boolean filtered;
	@Temporal(TemporalType.TIMESTAMP)
	private Date firstCommitDate;
	@JsonIgnore
	@OneToMany(mappedBy="gitRepository", cascade = CascadeType.REMOVE)
	private List<GitRepositoryVersion> gitRepositoryVersion;
	@Enumerated(EnumType.STRING)
	private FilteredEnum filteredReason;
	private String defaultBranch;
	private Integer numberStars;
	private String downloadVersionHash;
	@Temporal(TemporalType.TIMESTAMP)
	private Date downloadDate;
	private String cloneUrl;
	private boolean privateRepository;
	private int size;
	@javax.persistence.Transient
	private int numberAnalysedDevs, numberAllCommits, numberAllFiles;

	public GitRepository(String name, String currentFolderPath, String fullName, String downloadVersionHash) {
		this.name = name;
		this.currentFolderPath = currentFolderPath;
		this.fullName = fullName;
		this.downloadVersionHash = downloadVersionHash;
	}

	public GitRepository(String name, String fullName, String language, 
			String currentFolderPath, String defaultBranch, Integer numberStars, String downloadVersionHash) {
		super();
		this.name = name;
		this.fullName = fullName;
		this.language = language;
		this.currentFolderPath = currentFolderPath;
		this.defaultBranch = defaultBranch;
		this.numberStars = numberStars;
		this.downloadVersionHash = downloadVersionHash;
	}

	public GitRepositoryDTO toDto() {
		return GitRepositoryDTO.builder()
				.id(id)
				.currentFolderPath(currentFolderPath)
				.defaultBranch(defaultBranch)
				.firstCommitDate(firstCommitDate)
				.fullName(fullName)
				.mainLanguage(language)
				.name(name)
				.numberStars(numberStars)
				.build();
	}

	public GitRepository(String name, String fullName, boolean privateRepository) {
		super();
		this.name = name;
		this.fullName = fullName;
		this.privateRepository = privateRepository;
	}
}		