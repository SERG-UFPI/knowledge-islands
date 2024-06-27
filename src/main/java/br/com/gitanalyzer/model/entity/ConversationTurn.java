package br.com.gitanalyzer.model.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import br.com.gitanalyzer.model.github_openai.enums.ChatgptUserAgent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ConversationTurn {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Enumerated(EnumType.STRING)
	private ChatgptUserAgent userAgent;
	@Lob
	@Column(columnDefinition="TEXT")
	private String fullText;
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private List<PromptCode> codes;
	@ManyToOne
	private ChatgptConversation conversation;
	private Long createTime;
}
