package br.com.knowledgeislands.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TruckFactorProcessDTO {

	private Long id;
	private String startDate;
	private String endDate;
	private String stage;
	private TruckFactorDTO truckFactor;
	private UserDTO user;
	@NotBlank
	@Pattern(regexp="^(https:\\/\\/)?(www\\.)?(github\\.com|gitlab\\.com|bitbucket\\.org)\\/([a-zA-Z0-9-_]+\\/[a-zA-Z0-9-_]+)(\\.git)?$")
	private String repositoryUrl;

}
