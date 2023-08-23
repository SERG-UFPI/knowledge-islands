package br.com.gitanalyzer.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import br.com.gitanalyzer.enums.TimeIntervalTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilteringProjectsDTO {

	@NotBlank
	private String folderPath;
	@NotNull
	private TimeIntervalTypeEnum intervalType;
	private int interval;
}
