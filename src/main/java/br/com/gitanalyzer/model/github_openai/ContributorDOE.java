package br.com.gitanalyzer.model.github_openai;

import br.com.gitanalyzer.model.entity.Contributor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContributorDOE {
	private Contributor contributor;
	private double doe;
}
