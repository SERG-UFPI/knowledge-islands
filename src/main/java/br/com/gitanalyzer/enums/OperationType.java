package br.com.gitanalyzer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OperationType {
	ADD('A'), MOD('M'), DEL('D'), REN('R');

	private char operationType;

}
