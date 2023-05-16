package br.com.gitanalyzer.model;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

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
	@ElementCollection
	private List<String> implicatedFiles;

	public TruckFactor(int truckfactor,
			ProjectVersion projectVersion, KnowledgeMetric knowledgeMetric, List<String> implicatedFiles) {
		this.truckfactor = truckfactor;
		this.projectVersion = projectVersion;
		this.knowledgeMetric = knowledgeMetric;
		this.implicatedFiles = implicatedFiles;
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
				.implicatedFiles(implicatedFiles)
				.knowledgeMetric(knowledgeMetric)
				.truckfactor(truckfactor)
				.build();
	}
}
