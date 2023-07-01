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
	private Date startDate;
	private Date endDate;
	private StageEnum stage;
	private TruckFactorDTO truckFactor;
	private UserDTO user;

}
