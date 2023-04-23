package br.com.gitanalyzer.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
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
	@Column(unique=true)
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

	public Project(String name) {
		this.name = name;
	}

	public Project(String name, String fullName, String mainLanguage, String currentPath, String defaultBranch) {
		super();
		this.name = name;
		this.fullName = fullName;
		this.mainLanguage = mainLanguage;
		this.currentPath = currentPath;
		this.defaultBranch = defaultBranch; 
	}

}
