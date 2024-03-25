package br.com.gitanalyzer.model.github_openai;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationTurn {
	private ChatgptUserAgent userAgent;
	private String fullText;
	private List<PromptCode> codes;
	private Long createTime;
}
