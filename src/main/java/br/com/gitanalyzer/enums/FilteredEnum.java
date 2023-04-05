package br.com.gitanalyzer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilteredEnum {

	SIZE("Size"),
	HISTORY_MIGRATION("History");
	
	private String name;
}
