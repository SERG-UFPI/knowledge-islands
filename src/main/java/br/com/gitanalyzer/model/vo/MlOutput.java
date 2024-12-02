package br.com.gitanalyzer.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MlOutput {

	private String author;
	private String file;
	private String expertise;
}
