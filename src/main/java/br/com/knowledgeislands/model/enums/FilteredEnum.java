package br.com.knowledgeislands.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilteredEnum {

	SIZE("Size"),
	HISTORY_MIGRATION("History"),
	NOT_SOFTWARE_PROJECT("Not a sofware project"),
	PROJECT_AGE("Project age"),
	NOT_THE_ANALYZED_LANGUAGE("Not the analyzed language"),
	INACTIVE_PROJECT("Inactive project");
	
	private String name;
}
