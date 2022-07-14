package br.com.gitanalyzer.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.com.gitanalyzer.enums.KnowledgeMetric;
import lombok.Data;

@Data
@Entity
public class TruckFactor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private int numberAllDevs, numberAnalysedDevs, numberAnalysedDevsAlias, 
	numberAllFiles, numberAnalysedFiles, numberAllCommits, numberAnalysedCommits, truckfactor;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateVersion; 
	@Enumerated(EnumType.STRING)
	private KnowledgeMetric knowledgeMetric;
	@ManyToOne
	private Project project;

	public TruckFactor(int numberAllDevs, int numberAnalysedDevs, int numberAnalysedDevsAlias, int numberAllFiles,
			int numberAnalysedFiles, int numberAllCommits, int numberAnalysedCommits, int truckfactor,
			Project project, Date dateVersion, KnowledgeMetric knowledgeMetric) {
		this.numberAllDevs = numberAllDevs;
		this.numberAnalysedDevs = numberAnalysedDevs;
		this.numberAnalysedDevsAlias = numberAnalysedDevsAlias;
		this.numberAllFiles = numberAllFiles;
		this.numberAnalysedFiles = numberAnalysedFiles;
		this.numberAllCommits = numberAllCommits;
		this.numberAnalysedCommits = numberAnalysedCommits;
		this.truckfactor = truckfactor;
		this.project = project;
		this.dateVersion = dateVersion;
		this.knowledgeMetric = knowledgeMetric;
	}
}
