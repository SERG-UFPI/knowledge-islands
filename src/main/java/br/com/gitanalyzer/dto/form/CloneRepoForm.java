package br.com.gitanalyzer.dto.form;

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
	public String url;
	@NotNull
	public Long idUser;
	public String branch;
}
