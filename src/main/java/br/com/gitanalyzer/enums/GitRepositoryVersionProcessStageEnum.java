package br.com.gitanalyzer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GitRepositoryVersionProcessStageEnum {
	INITIALIZED("Process initialized"), DOWNLOADING("Downloading repository"), 
	EXTRACTING_DATA("Extracting history data"), EXTRACTION_FINISHED("Extraction finished");

	private String name;
}
