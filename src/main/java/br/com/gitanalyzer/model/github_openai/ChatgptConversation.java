package br.com.gitanalyzer.model.github_openai;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatgptConversation {
	private List<ConversationTurn> conversationTurns;
	private Long createTime;
	private Long updateTime;

	public ChatgptConversation() {
		conversationTurns = new ArrayList<>();
	}
}
