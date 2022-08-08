package br.com.gitanalyzer.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class TruckFactorDevelopers {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name, email;
	@ManyToOne
	private TruckFactor truckFactor;

	public TruckFactorDevelopers(String name, String email, TruckFactor truckFactor) {
		super();
		this.name = name;
		this.email = email;
		this.truckFactor = truckFactor;
	}

}
