package br.com.knowledgeislands.model.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QualityMeasures {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Double cbo;
	private Double dit;
	private Double noc;
	private Double lcom;
	private Double rfc;
	private Double wmc;
	
	public QualityMeasures(Double cbo, Double dit, Double noc, Double lcom, Double rfc, Double wmc) {
		super();
		this.cbo = cbo;
		this.dit = dit;
		this.noc = noc;
		this.lcom = lcom;
		this.rfc = rfc;
		this.wmc = wmc;
	}
}
