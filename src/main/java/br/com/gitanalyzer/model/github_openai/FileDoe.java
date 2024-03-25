package br.com.gitanalyzer.model.github_openai;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileDoe {
	private File file;
	private double doe;
	public FileDoe() {
		file = new File();
	}
}
