package br.com.gitanalyzer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StageEnum {

	DOWNLOAD("Downloading"), EXTRACT_DATA("Extracting Data"), CALCULATING("Calculating Truck Factor"), 
	ANALYSIS_FINISHED("Analysis finished");

	private String name;
}
