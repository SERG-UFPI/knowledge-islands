package br.com.knowledgeislands.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum KnowledgeModel {
	DOA("DOA"), DOE("DOE"), MACHINE_LEARNING("Machine Learning");

	private String name;
	
}
