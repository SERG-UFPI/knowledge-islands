package br.com.knowledgeislands.dto.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDTO {
	private String filePath;
	private String repository;
	private String commitAddedLink;
	private String copiedCode;
}
