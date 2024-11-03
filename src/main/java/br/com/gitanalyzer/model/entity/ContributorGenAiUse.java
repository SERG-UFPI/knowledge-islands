package br.com.gitanalyzer.model.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ContributorGenAiUse {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToOne
	private Contributor contributor;
	private int totalNumCopiedLines;
	private double avgCopiedLines;
	public ContributorGenAiUse(Contributor contributor, int totalNumCopiedLines, double avgCopiedLines) {
		super();
		this.contributor = contributor;
		this.totalNumCopiedLines = totalNumCopiedLines;
		this.avgCopiedLines = avgCopiedLines;
	}
}
