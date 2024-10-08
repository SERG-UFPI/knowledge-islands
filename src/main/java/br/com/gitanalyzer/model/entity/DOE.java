package br.com.gitanalyzer.model.entity;

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
	private int adds;
	private int fa;
	private int numDays;
	private int size;
	private double doeValue;
	
	public DOE(int adds, int fa, int numDays, int size, double doeValue) {
		super();
		this.adds = adds;
		this.fa = fa;
		this.numDays = numDays;
		this.size = size;
		this.doeValue = doeValue;
	}

	public DOE(int adds, int fa, int numDays) {
		super();
		this.adds = adds;
		this.fa = fa;
		this.numDays = numDays;
	}
}
