package br.com.gitanalyzer.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
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
	private String currentPath;
	private String mainLanguage;
	private boolean filtered;
	@Temporal(TemporalType.TIMESTAMP)
	private Date firstCommitDate;
	@OneToMany(mappedBy="project", fetch=FetchType.LAZY)
	private List<ProjectVersion> versions;
	private FilteredEnum filteredReason;

	public Project(String name) {
		this.name = name;
	}

	public Project(String name, String mainLanguage, String currentPath) {
		super();
		this.name = name;
		this.mainLanguage = mainLanguage;
	}

}
