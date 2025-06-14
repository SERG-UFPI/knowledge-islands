package br.com.knowledgeislands.model.entity;

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
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.knowledgeislands.model.enums.SharedLinkSourceType;
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
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private ChatGptConversation conversation;
	@JsonIgnore
	@Enumerated(EnumType.STRING)
	private SharedLinkSourceType type;
	@JsonIgnore
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private SharedLinkErrorLog error;
	@Transient
	private List<SharedLinkCommit> sharedLinkCommits;

	public SharedLink(String link, String textMatchFragment) {
		super();
		this.link = link;
		this.textMatchFragment = textMatchFragment;
	}

	public SharedLink(String link, String textMatchFragment, ChatGptConversation conversation) {
		super();
		this.link = link;
		this.textMatchFragment = textMatchFragment;
		this.conversation = conversation;
	}

}
