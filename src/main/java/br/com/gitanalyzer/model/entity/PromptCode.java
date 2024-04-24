package br.com.gitanalyzer.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class PromptCode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String language;
	@Lob
	@Column(columnDefinition="TEXT")
	private String codeFullText;
	@Lob
	@Column(columnDefinition="TEXT")
	private String code;
	private String codeId;

	public static String startOfCodeBlockId() {
		return "CODE_BLOCK_";
	}

	public PromptCode(String language, String codeFullText, String code, String codeId) {
		super();
		this.language = language;
		this.codeFullText = codeFullText;
		this.code = code;
		this.codeId = codeId;
	}

}
