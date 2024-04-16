package br.com.gitanalyzer.model.github_openai;

import br.com.gitanalyzer.model.DOE;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileAuthor {
	private File file;
	private DOE doe;
	public FileAuthor() {
		file = new File();
	}
}
