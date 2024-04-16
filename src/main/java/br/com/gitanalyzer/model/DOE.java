package br.com.gitanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DOE {

	private double adds;
	private double fa;
	private double numDays;
	private double size;
	private double doe;

}
