package br.com.gitanalyzer.model.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import br.com.gitanalyzer.dto.TruckFactorDevelopersDTO;
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
	private double percentOfFilesAuthored;

	public TruckFactorDevelopers(String name, String email, TruckFactor truckFactor, double percentOfFilesAuthored) {
		super();
		this.name = name;
		this.email = email;
		this.truckFactor = truckFactor;
		this.percentOfFilesAuthored = percentOfFilesAuthored;
	}

	public TruckFactorDevelopersDTO toDto() {
		return TruckFactorDevelopersDTO.builder()
				.email(email).id(id).name(name).percentOfFilesAuthored(percentOfFilesAuthored)
				.build();
	}
}
