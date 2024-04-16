package br.com.gitanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DOA {
	private double faModel;
	private double dlModel;
	private double acModel;
	private double doa;
}
