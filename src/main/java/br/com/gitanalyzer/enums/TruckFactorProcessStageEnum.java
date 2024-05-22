package br.com.gitanalyzer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TruckFactorProcessStageEnum {

	INITIALIZED("Process initialized"), DOWNLOADING("Downloading repository"), 
	EXTRACTING_DATA("Extracting history data"), CALCULATING("Calculating Truck Factor"), 
	ANALYSIS_FINISHED("Analysis finished");

	private String name;
}
