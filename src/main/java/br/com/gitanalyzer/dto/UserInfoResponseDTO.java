package br.com.gitanalyzer.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponseDTO {
	
	private Long id;
	private String username;
	private String email;
	private List<String> roles;
	
}
