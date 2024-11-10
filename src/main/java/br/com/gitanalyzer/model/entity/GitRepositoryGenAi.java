package br.com.gitanalyzer.model.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GitRepositoryGenAi {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Double percentageFilesAffected;
	
	public GitRepositoryGenAi(Double percentageFilesAffected) {
		super();
		this.percentageFilesAffected = percentageFilesAffected;
	}
	
}
