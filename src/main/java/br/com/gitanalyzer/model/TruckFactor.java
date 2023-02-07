package br.com.gitanalyzer.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

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

	public TruckFactor(int truckfactor,
			ProjectVersion projectVersion, KnowledgeMetric knowledgeMetric) {
		this.truckfactor = truckfactor;
		this.projectVersion = projectVersion;
		this.knowledgeMetric = knowledgeMetric;
	}
}
