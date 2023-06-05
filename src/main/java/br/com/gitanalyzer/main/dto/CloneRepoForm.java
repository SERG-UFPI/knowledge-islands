package br.com.gitanalyzer.main.dto;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CloneRepoForm {

	@NotNull
	public String url;
	public String branch;
}
