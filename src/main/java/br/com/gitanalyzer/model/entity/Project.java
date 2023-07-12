package br.com.gitanalyzer.model.entity;

import java.util.Date;
import java.util.List;

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
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String fullName;
	private String currentPath;
	private String mainLanguage;
	private boolean filtered;
	@Temporal(TemporalType.TIMESTAMP)
	private Date firstCommitDate;
	@OneToMany(mappedBy="project", fetch=FetchType.LAZY)
	private List<ProjectVersion> versions;
	@Enumerated(EnumType.STRING)
	private FilteredEnum filteredReason;
	private String defaultBranch;
	private Integer numberStars;

	@javax.persistence.Transient
	private int numberAnalysedDevs, numberAllCommits, numberAllFiles;

	public Project(String name, String currentPath, String fullName) {
		this.name = name;
		this.currentPath = currentPath;
		this.fullName = fullName;
	}

	public Project(String name, String fullName, String mainLanguage, 
			String currentPath, String defaultBranch, Integer numberStars) {
		super();
		this.name = name;
		this.fullName = fullName;
		this.mainLanguage = mainLanguage;
		this.currentPath = currentPath;
		this.defaultBranch = defaultBranch;
		this.numberStars = numberStars;
	}

	public ProjectDTO toDto() {
		return ProjectDTO.builder()
				.id(id)
				.currentPath(currentPath)
				.defaultBranch(defaultBranch)
				.firstCommitDate(firstCommitDate)
				.fullName(fullName)
				.mainLanguage(mainLanguage)
				.name(name)
				.numberStars(numberStars)
				.build();
	}
}		