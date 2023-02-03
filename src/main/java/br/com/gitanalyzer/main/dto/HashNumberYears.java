package br.com.gitanalyzer.main.dto;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HashNumberYears {
	
	@NotNull
	private String path;
	@NotNull
	private int numberYears;
}
