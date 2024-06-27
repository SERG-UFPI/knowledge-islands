package br.com.gitanalyzer.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.com.gitanalyzer.model.github_openai.enums.SharedLinkSourceType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Entity
@AllArgsConstructor
public class SharedLinkSearch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	@Enumerated(EnumType.STRING)
	private SharedLinkSourceType searchType;
	@Column(length = 500)
	private String searchCall;
	private int totalNumberOfItems;
	public SharedLinkSearch() {
		super();
		this.date = new Date();
	}

}
