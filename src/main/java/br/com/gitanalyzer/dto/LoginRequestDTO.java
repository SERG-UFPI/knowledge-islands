package br.com.gitanalyzer.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {

	@NotNull
	@NotEmpty
	private String username;
	@NotNull
	@NotEmpty
	private String password;

}
