package br.com.gitanalyzer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LanguageEnum {

	JAVASCRIPT("javascript"), PYTHON("python"), JAVA("java"), TYPESCRIPT("typescript"), C_SHARP("csharp"), C_PLUS_PLUS("c++"), ALL("all");
	private String name;

}
