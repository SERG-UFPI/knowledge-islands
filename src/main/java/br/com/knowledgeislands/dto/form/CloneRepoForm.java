package br.com.knowledgeislands.dto.form;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CloneRepoForm {

	@NotNull
	public String cloneUrl;
	public Long idUser;
	public String branch;
}
