package br.com.gitanalyzer.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ErrorLog {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(length = 500)
	private String artifact;
	@Column(length = 500)
	private String message;
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	public ErrorLog(String artifact, String message, Date date) {
		super();
		this.artifact = artifact;
		this.message = message;
		this.date = date;
	}

}
