package br.com.gitanalyzer.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LanguageEnum {

	JAVASCRIPT("javascript"), PYTHON("python"), JAVA("java"), TYPESCRIPT("typescript"), C_SHARP("csharp"), C_PLUS_PLUS("cpp"), ALL("all");
	private String name;

}
