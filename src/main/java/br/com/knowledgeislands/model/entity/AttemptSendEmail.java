package br.com.knowledgeislands.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class AttemptSendEmail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	private boolean success;
	@Column(length=1000)
	private String error;
	@ManyToOne
	private Contributor contributor;
	public AttemptSendEmail(Date date, Contributor contributor) {
		super();
		this.date = date;
		this.contributor = contributor;
	}

}
