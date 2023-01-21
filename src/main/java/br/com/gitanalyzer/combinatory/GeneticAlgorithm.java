package br.com.gitanalyzer.combinatory;

import java.util.List;
import java.util.function.Function;

import br.com.gitanalyzer.model.File;
import io.jenetics.EnumGene;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;

public class GeneticAlgorithm implements Problem<ISeq<Integer>, EnumGene<Integer>, Integer> {
	
	private List<File> files;

	@Override
	public Function<ISeq<Integer>, Integer> fitness() {
		return null;
	}

	@Override
	public Codec<ISeq<Integer>, EnumGene<Integer>> codec() {
		return null;
	}

}
