package br.com.gitanalyzer.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OperationType {
	ADDED, MODIFIED, RENAMED;
}
