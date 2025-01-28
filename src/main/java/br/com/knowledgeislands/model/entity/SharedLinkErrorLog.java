package br.com.knowledgeislands.model.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import br.com.knowledgeislands.model.enums.SharedLinkFetchError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class SharedLinkErrorLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Enumerated(EnumType.STRING)
	private SharedLinkFetchError errorType;
	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private ErrorLog errorLog;
	public SharedLinkErrorLog(SharedLinkFetchError errorType, ErrorLog errorLog) {
		super();
		this.errorType = errorType;
		this.errorLog = errorLog;
	}
}
