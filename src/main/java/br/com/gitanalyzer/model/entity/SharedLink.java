package br.com.gitanalyzer.model.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.gitanalyzer.model.enums.SharedLinkSourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class SharedLink {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(unique=true)
	private String link;
	@Lob
	@Column(columnDefinition="TEXT")
	private String textMatchFragment;
	@Lob
	@Column(columnDefinition="TEXT")
	private String openAiFullJson;
	@JsonIgnore
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private ChatgptConversation conversation;
	@Enumerated(EnumType.STRING)
	private SharedLinkSourceType type;
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private SharedLinkErrorLog error;

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
