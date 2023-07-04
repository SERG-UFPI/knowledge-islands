package br.com.gitanalyzer.dto.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloaderForm {
	
	private String path, token;
	private int numRepository;

}
