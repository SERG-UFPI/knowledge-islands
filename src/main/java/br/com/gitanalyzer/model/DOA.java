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
	private double faModel;
	private double dlModel;
	private double acModel;
	private double doa;
	public DOA(double faModel, double dlModel, double acModel, double doa) {
		super();
		this.faModel = faModel;
		this.dlModel = dlModel;
		this.acModel = acModel;
		this.doa = doa;
	}
}
