package br.com.knowledgeislands.model.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class GlobalGenAiUse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private int numCopiedCode;
	private int numContributorCopied;

	private double avgPctCopiedCode;

	public GlobalGenAiUse(int numCopiedCode, int numContributorCopied, double avgPctCopiedCode) {
		super();
		this.numCopiedCode = numCopiedCode;
		this.numContributorCopied = numContributorCopied;
		this.avgPctCopiedCode = avgPctCopiedCode;
	}

}
