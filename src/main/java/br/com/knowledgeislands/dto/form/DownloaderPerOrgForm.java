package br.com.knowledgeislands.dto.form;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloaderPerOrgForm {

	@NotBlank
	private String path;
	@NotBlank
	private String org;

}
