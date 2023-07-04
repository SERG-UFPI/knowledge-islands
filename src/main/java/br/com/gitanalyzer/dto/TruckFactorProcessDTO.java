package br.com.gitanalyzer.dto;

import java.util.Date;

import br.com.gitanalyzer.enums.StageEnum;
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
	private String repositoryUrl;

}
