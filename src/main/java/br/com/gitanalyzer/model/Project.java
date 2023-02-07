package br.com.gitanalyzer.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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

	public Project(String name) {
		this.name = name;
	}

	public Project(String name, String mainLanguage) {
		super();
		this.name = name;
		this.mainLanguage = mainLanguage;
	}

}
