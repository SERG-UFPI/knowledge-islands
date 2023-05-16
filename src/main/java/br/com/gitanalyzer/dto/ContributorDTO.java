package br.com.gitanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContributorDTO {

	private String name;
	private String email;
	private int numberFilesAuthor;
	private double sumFileImportance;

}
