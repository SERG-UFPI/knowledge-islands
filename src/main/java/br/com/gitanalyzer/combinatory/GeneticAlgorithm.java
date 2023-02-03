//package br.com.gitanalyzer.combinatory;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import br.com.gitanalyzer.model.File;
//import io.jenetics.Genotype;
//import io.jenetics.IntegerChromosome;
//import io.jenetics.IntegerGene;
//import io.jenetics.engine.Engine;
//import io.jenetics.util.Factory;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class GeneticAlgorithm extends CombinatoryProblem{
//	
//	public GeneticAlgorithm(List<File> files, int contributorsSize) {
//		this.files = files;
//		this.contributorsSize = contributorsSize;
//		setProblem();
//	}
//	
//	private static int eval(Genotype<IntegerGene> gt) {
//		int[] filesAbandonedIndexes = gt.chromosome().as(IntegerChromosome.class).toArray();
//		return 0;
//	}
//	
//	private IntegerChromosome getChromossomeFromSolution() {
//		int[] solutionCheck = new int[solutionLength];
//		List<IntegerGene> genes = new ArrayList<>();
//		for (int i = 0; i < solutionLength; i++) {
//			int value = 0;
//			IntegerGene gene = null;
//			while(true) {
//				gene = IntegerGene.of(0, files.size());
//				value = gene.intValue();
//				if(arrayContains(solutionCheck, value) == false) {
//					break;
//				}
//			}
//			solutionCheck[i] = value;
//			genes.add(gene);
//		}
//		return IntegerChromosome.of(genes);
//	}
//	
//	public void executeHillClimbing() {
//		log.info("EXECUTING GENETIC ALGORITHM");
//		Factory<Genotype<IntegerGene>> gtf = Genotype.of(getChromossomeFromSolution());
//		Engine<IntegerGene, Integer> engine = Engine.builder(GeneticAlgorithm::eval, gtf)
//				.populationSize(100)
//				.minimizing()
//				.alterers(null, null)
//				.build();
//		
//	}
//
//}
