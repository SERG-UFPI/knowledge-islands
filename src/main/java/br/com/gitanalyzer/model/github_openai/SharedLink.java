package br.com.gitanalyzer.model.github_openai;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SharedLink {
	private String link;
	private String textMatchFragment;
	private ChatgptConversation conversation;
	private String openAiFullJson;
	private Commit commitThatAddedTheLink;
	private List<String> linesCopied;
	public SharedLink(String link, String textMatchFragment) {
		super();
		this.link = link;
		this.textMatchFragment = textMatchFragment;
	}
	public SharedLink(String link, String textMatchFragment, ChatgptConversation conversation, String openAiFullJson) {
		super();
		this.link = link;
		this.textMatchFragment = textMatchFragment;
		this.conversation = conversation;
		this.openAiFullJson = openAiFullJson;
	}
}
