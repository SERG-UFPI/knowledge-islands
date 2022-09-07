package br.com.gitanalyzer.main.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TruckFactorDevelopersVO {

	private String name, email, project, dateVersion, knowledgeMetric;

	public TruckFactorDevelopersVO(String name, String email, String project, String dateVersion, String knowledgeMetric) {
		super();
		this.name = name;
		this.email = email;
		this.project = project;
		this.dateVersion = dateVersion;
		this.knowledgeMetric = knowledgeMetric;
	}

	@Override
	public String toString() {
		return name+";"+email+";"+project+";"+dateVersion+";"+knowledgeMetric;
	}

}
