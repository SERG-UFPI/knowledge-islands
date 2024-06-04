package br.com.gitanalyzer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GitRepositoryVersionProcessStageEnum {
	INITIALIZED("Process started"), DOWNLOADING("Downloading repository"), 
	EXTRACTING_DATA("Extracting history data"), EXTRACTION_FINISHED("Process finished");

	private String name;
}

