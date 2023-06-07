package br.com.gitanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TruckFactorDevelopersDTO {
	
	private Long id;
	private String name, email;
	private double percentOfFilesAuthored;
	
}
