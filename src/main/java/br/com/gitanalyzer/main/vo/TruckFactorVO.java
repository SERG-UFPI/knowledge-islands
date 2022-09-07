package br.com.gitanalyzer.main.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TruckFactorVO {

	private int numberAllDevs, numberAnalysedDevs, numberAnalysedDevsAlias, 
	numberAllFiles, numberAnalysedFiles, numberAllCommits, numberAnalysedCommits, truckfactor;
	private String projectName, dateVersion, knowledgeMetric;

	public TruckFactorVO(int numberAllDevs, int numberAnalysedDevs, int numberAnalysedDevsAlias, int numberAllFiles,
			int numberAnalysedFiles, int numberAllCommits, int numberAnalysedCommits, int truckfactor,
			String projectName, String dateVersion, String knowledgeMetric) {
		super();
		this.numberAllDevs = numberAllDevs;
		this.numberAnalysedDevs = numberAnalysedDevs;
		this.numberAnalysedDevsAlias = numberAnalysedDevsAlias;
		this.numberAllFiles = numberAllFiles;
		this.numberAnalysedFiles = numberAnalysedFiles;
		this.numberAllCommits = numberAllCommits;
		this.numberAnalysedCommits = numberAnalysedCommits;
		this.truckfactor = truckfactor;
		this.projectName = projectName;
		this.dateVersion = dateVersion;
		this.knowledgeMetric = knowledgeMetric;
	}

	@Override
	public String toString() {
		return numberAllDevs + ";" + numberAnalysedDevs
				+ ";" + numberAnalysedDevsAlias + ";" + numberAllFiles
				+ ";" + numberAnalysedFiles + ";" + numberAllCommits
				+ ";" + numberAnalysedCommits + ";" + truckfactor + ";"
				+ projectName + ";" + dateVersion+";"+knowledgeMetric;
	}

}
