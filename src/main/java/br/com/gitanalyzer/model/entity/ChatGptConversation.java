package br.com.gitanalyzer.model.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Entity
@AllArgsConstructor
public class ChatGptConversation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToMany(mappedBy = "conversation", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private List<ConversationTurn> conversationTurns;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date updateTime;
	private String title;

	public ChatGptConversation() {
		conversationTurns = new ArrayList<>();
	}

	public void addConversationTurn(ConversationTurn turn) {
		turn.setConversation(this);
		conversationTurns.add(turn);
	}
}
