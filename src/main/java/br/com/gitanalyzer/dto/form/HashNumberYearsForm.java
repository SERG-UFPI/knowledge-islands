package br.com.gitanalyzer.dto.form;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HashNumberYearsForm {
	
	@NotNull
	private String path;
	@NotNull
	private int numberYears;
}
