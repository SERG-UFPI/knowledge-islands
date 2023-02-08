package br.com.gitanalyzer.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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

	public Project(String name) {
		this.name = name;
	}

	public Project(String name, String mainLanguage) {
		super();
		this.name = name;
		this.mainLanguage = mainLanguage;
	}

}
