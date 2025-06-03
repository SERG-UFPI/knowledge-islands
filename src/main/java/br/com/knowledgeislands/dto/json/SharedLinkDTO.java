package br.com.knowledgeislands.dto.json;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedLinkDTO {
	private String sharedLink;
	private String title;
	private ConversationDTO conversation;
	private List<FileDTO> files;
}
