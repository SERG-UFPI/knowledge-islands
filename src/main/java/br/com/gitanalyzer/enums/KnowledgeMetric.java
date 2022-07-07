package br.com.gitanalyzer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum KnowledgeMetric {
	DOA("DOA"), DOE("DOE");

	private String name;

}
