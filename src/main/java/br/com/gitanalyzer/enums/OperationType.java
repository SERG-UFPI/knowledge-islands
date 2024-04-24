package br.com.gitanalyzer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OperationType {
	ADD("ADDED"), MOD("MODIFIED"), REN("RENAMED");

	private String operationType;

	public static OperationType getEnumByType(String operationType) {
		for (OperationType operation : OperationType.values()) {
			if (operation.getOperationType().toUpperCase().equals(operationType)) {
				return operation;
			}
		}
		return null;
	}

}
