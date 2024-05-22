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
public class DOA {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private int fa;
	private int dl;
	private int ac;
	private double doa;
	public DOA(int fa, int dl, int ac, double doa) {
		super();
		this.fa = fa;
		this.dl = dl;
		this.ac = ac;
		this.doa = doa;
	}
	public DOA(int fa, int dl, int ac) {
		super();
		this.fa = fa;
		this.dl = dl;
		this.ac = ac;
	}
}
