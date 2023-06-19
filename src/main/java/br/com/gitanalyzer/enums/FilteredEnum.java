package br.com.gitanalyzer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilteredEnum {

	SIZE("Size"),
	HISTORY_MIGRATION("History"),
	NOT_SOFTWARE_PROJECT("Not a sofware project"),
	PROJECT_AGE("Project age");
	
	private String name;
}
