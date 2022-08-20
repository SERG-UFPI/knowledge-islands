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
	private int numberAnalysedDevs, numberAuthors, numberAnalysedDevsAlias, 
	numberAllFiles, numberAnalysedFiles, numberAllCommits, numberAnalysedCommits, truckfactor;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateVersion; 
	private String versionId;
	@Enumerated(EnumType.STRING)
	private KnowledgeMetric knowledgeMetric;
	@ManyToOne
	private Project project;

	public TruckFactor(int numberAnalysedDevs, int numberAuthors, int numberAnalysedDevsAlias, int numberAllFiles,
			int numberAnalysedFiles, int numberAllCommits, int numberAnalysedCommits, int truckfactor,
			Project project, Date dateVersion, String versionId, KnowledgeMetric knowledgeMetric) {
		this.numberAuthors = numberAuthors;
		this.numberAnalysedDevs = numberAnalysedDevs;
		this.numberAnalysedDevsAlias = numberAnalysedDevsAlias;
		this.numberAllFiles = numberAllFiles;
		this.numberAnalysedFiles = numberAnalysedFiles;
		this.numberAllCommits = numberAllCommits;
		this.numberAnalysedCommits = numberAnalysedCommits;
		this.truckfactor = truckfactor;
		this.project = project;
		this.dateVersion = dateVersion;
		this.versionId = versionId;
		this.knowledgeMetric = knowledgeMetric;
	}
}
