package br.com.knowledgeislands.dto;

import java.util.List;

import br.com.knowledgeislands.model.entity.Commit;
import br.com.knowledgeislands.model.entity.ContributorVersion;
import br.com.knowledgeislands.model.entity.FileVersion;
import br.com.knowledgeislands.model.enums.KnowledgeModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAuthorFileExpertiseDTO {

	private KnowledgeModel knowledgeMetric; 
	private List<Commit> commits;
	private ContributorVersion contributorVersion; 
	private FileVersion fileVersion;
	private boolean genAi;
}
