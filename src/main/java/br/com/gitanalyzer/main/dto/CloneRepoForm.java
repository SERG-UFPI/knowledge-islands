package br.com.gitanalyzer.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CloneRepoForm {

	public String url;
	public String branch;
}
