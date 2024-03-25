package br.com.gitanalyzer.model.github_openai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromptCode {
	private String language;
	private String codeFullText;
	private String code;
	private String codeId;
	
	public static String startOfCodeBlockId() {
		return "CODE_BLOCK_";
	}
}
