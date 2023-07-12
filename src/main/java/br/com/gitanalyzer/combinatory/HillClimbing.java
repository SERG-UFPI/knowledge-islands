package br.com.gitanalyzer.combinatory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import br.com.gitanalyzer.model.Solution;
import br.com.gitanalyzer.model.entity.File;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HillClimbing extends CombinatoryProblem{
	
	public HillClimbing(List<File> files, int contributorsSize) {
		this.files = files;
		this.contributorsSize = contributorsSize;
		setProblem();
	}
	
//	public void executeHillClimbing() {
//		log.info("EXECUTING HILL CLIMBING");
//		int shotGunNumber = 200;
//		int maxIterations = 100;
//		List<Solution> listOfBestSolutions = new ArrayList<>();
//		for(int i = 0; i < shotGunNumber; i++) {
//			int[] solution = getSolution();
//			boolean flag = false;
//			int j = 0;
//			int[] backupSolution = new int[solution.length];
//			while(flag == false && j < maxIterations) {
//				backupSolution = Arrays.copyOf(solution, solution.length);
//				solution = getBetterSolution(solution);
//				if(solution == null) {
//					flag = true;
//				}
//				j++;
//			}
//			listOfBestSolutions.add(new Solution(backupSolution, checkSolution(backupSolution)));
//			i++;
//		}
//		printBestSolution(listOfBestSolutions);
//		System.out.println();
//	}
	
	private void printBestSolution(List<Solution> listOfBestSolutions) {
		Solution bestSolution = listOfBestSolutions.get(0);
		for(Solution solution: listOfBestSolutions) {
			if(solution.getFitness() < bestSolution.getFitness()) {
				bestSolution = solution;
			}
		}
		log.info("BEST SOLUTION: "+bestSolution.getFitness());
	}
//
//	public int[] getBetterSolution(int[] currentSolution) {
//		List<int[]> neighbors = new ArrayList<>();
//		Random rd = new Random();
//		for(int i = 0; i < currentSolution.length; i++) {
//			boolean flag = false;
//			while(flag == false) {
//				Integer randomFileIndex =  rd.nextInt(files.size());
//				if(arrayContains(currentSolution, randomFileIndex)) {
//					continue;
//				}else {
//					flag = true;
//				}
//				int[] neighbor = Arrays.copyOf(currentSolution, currentSolution.length);
//				neighbor[i] = randomFileIndex;
//				neighbors.add(neighbor);
//			}
//		}
//		int bestFit = checkSolution(currentSolution);
//		int[] solution = null;
//		for(int i = 0; i < neighbors.size(); i++) {
//			int fitness = checkSolution(neighbors.get(i));
//			if(fitness < bestFit) {
//				bestFit = fitness;
//				solution = neighbors.get(i);
//			}
//		}
//		return solution;
//	}
	
	public boolean checkIfTwoSolutionsEqual(Integer[] solution1, Integer[] solution2) {
		Arrays.sort(solution1);
		Arrays.sort(solution2);
		return solution1.equals(solution2);
	}
	
}
