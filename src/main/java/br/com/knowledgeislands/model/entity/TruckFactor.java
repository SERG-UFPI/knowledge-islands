package br.com.knowledgeislands.model.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class TruckFactor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private int truckFactor;
	@OneToMany(cascade = {CascadeType.MERGE, CascadeType.REMOVE})
	private List<ContributorVersion> contributors;
	@JsonIgnore
	@OneToOne
	private GitRepositoryVersionKnowledgeModel gitRepositoryVersionKnowledgeModel;
	@JsonIgnore
	@ManyToMany(cascade = {CascadeType.PERSIST})
	private List<FileVersion> implicatedFiles;
	private double timeToCalculate;

	public TruckFactor(int truckfactor, GitRepositoryVersionKnowledgeModel gitRepositoryVersionKnowledgeModel, List<FileVersion> implicatedFiles, List<ContributorVersion> contributors, Double timeToCalculate) {
		this.truckFactor = truckfactor;
		this.implicatedFiles = implicatedFiles;
		this.contributors = contributors;
		this.timeToCalculate = timeToCalculate;
		this.gitRepositoryVersionKnowledgeModel = gitRepositoryVersionKnowledgeModel;
	}

	public TruckFactor(int truckfactor) {
		this.truckFactor = truckfactor;
	}

	//	public TruckFactorDTO toDto() {
	//		return TruckFactorDTO.builder()
	//				.id(id)
	//				.projectVersion(repositoryVersion.toDto())
	//				.implicatedFiles(implicatedFiles)
	//				.knowledgeMetric(knowledgeModel)
	//				.truckfactor(truckfactor)
	////				.truckFactorDevelopers(contributors!=null?
	////						contributors.stream().map(t -> t.toDto()).toList():null)
	//				.build();
	//	}
}
