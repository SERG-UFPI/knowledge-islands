package br.com.gitanalyzer.model.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import br.com.gitanalyzer.dto.TruckFactorDTO;
import br.com.gitanalyzer.enums.KnowledgeMetric;
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
	private int truckfactor;
	@Enumerated(EnumType.STRING)
	private KnowledgeMetric knowledgeMetric;
	@ManyToOne
	private ProjectVersion projectVersion;
	@OneToMany(cascade = CascadeType.MERGE)
	private List<Contributor> contributors;
	@ElementCollection
	private List<String> implicatedFiles;

	public TruckFactor(int truckfactor,
			ProjectVersion projectVersion, KnowledgeMetric knowledgeMetric,
			List<String> implicatedFiles, List<Contributor> contributors) {
		this.truckfactor = truckfactor;
		this.projectVersion = projectVersion;
		this.knowledgeMetric = knowledgeMetric;
		this.implicatedFiles = implicatedFiles;
		this.contributors = contributors;
	}

	public TruckFactor(int truckfactor,
			ProjectVersion projectVersion, KnowledgeMetric knowledgeMetric) {
		this.truckfactor = truckfactor;
		this.projectVersion = projectVersion;
		this.knowledgeMetric = knowledgeMetric;
		//this.implicatedFiles = implicatedFiles;
	}

	public TruckFactorDTO toDto() {
		return TruckFactorDTO.builder()
				.id(id)
				.projectVersion(projectVersion.toDto())
				.implicatedFiles(implicatedFiles)
				.knowledgeMetric(knowledgeMetric)
				.truckfactor(truckfactor)
				.truckFactorDevelopers(contributors!=null?
						contributors.stream().map(t -> t.toDto()).toList():null)
				.build();
	}
}
