package br.com.gitanalyzer.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Data
@Entity
public class TruckFactorDevelopers {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name, email;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateVersion;	
	@ManyToOne
	private TruckFactor truckFactor;

	public TruckFactorDevelopers(String name, String email, Date dateVersion, TruckFactor truckFactor) {
		super();
		this.name = name;
		this.email = email;
		this.dateVersion = dateVersion;
		this.truckFactor = truckFactor;
	}

}
