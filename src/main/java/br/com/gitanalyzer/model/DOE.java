package br.com.gitanalyzer.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class DOE {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private double adds;
	private double fa;
	private double numDays;
	private double size;
	private double doe;
	public DOE(double adds, double fa, double numDays, double size, double doe) {
		super();
		this.adds = adds;
		this.fa = fa;
		this.numDays = numDays;
		this.size = size;
		this.doe = doe;
	}
	
}
