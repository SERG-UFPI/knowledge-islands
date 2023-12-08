package br.com.gitanalyzer.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContributorDTO {

	private Long id;
	private String name;
	private String email;
	private boolean active;
	private int numberFilesAuthor;
	private BigDecimal percentOfFilesAuthored;

}
