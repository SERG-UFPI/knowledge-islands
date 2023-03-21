package br.com.gitanalyzer.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Developer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name, email;
	private double percentOfFilesAuthored;
	@ManyToOne
	private ProjectVersion projectVersion;

	public Developer(String name, String email, double percentOfFilesAuthored, ProjectVersion projectVersion) {
		super();
		this.name = name;
		this.email = email;
		this.percentOfFilesAuthored = percentOfFilesAuthored;
		this.projectVersion = projectVersion;
	}

}
