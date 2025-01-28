package br.com.knowledgeislands.model.entity;

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
public class GitRepositoryDependency {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String packageName;
	private String packageManager;
	private String repositoryFullName;
	public GitRepositoryDependency(String packageName, String packageManager, String repositoryFullName) {
		super();
		this.packageName = packageName;
		this.packageManager = packageManager;
		this.repositoryFullName = repositoryFullName;
	}
	
}
