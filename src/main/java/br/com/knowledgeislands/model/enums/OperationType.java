package br.com.knowledgeislands.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OperationType {
	ADDED, MODIFIED, RENAMED;
}
