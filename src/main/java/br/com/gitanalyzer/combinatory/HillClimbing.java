package br.com.gitanalyzer.combinatory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import br.com.gitanalyzer.model.Contributor;
import br.com.gitanalyzer.model.File;
import br.com.gitanalyzer.model.Solution;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class HillClimbing {
	
	private List<File> files;
	private List<File> abandonedFiles;
	private int contributorsSize;
	
	public HillClimbing(List<File> files, int contributorsSize) {
		this.files = files;
		this.contributorsSize =contributorsSize;
		removeAbandonedFiles();
		indexFiles();
	}
	
	private void removeAbandonedFiles() {
		abandonedFiles = files.stream().filter(f -> f.getMantainers() == null || f.getMantainers().size() == 0).toList();
		if(abandonedFiles == null) {
			abandonedFiles = new ArrayList<>();
		}
		files = files.stream().filter(f -> f.getMantainers() != null && f.getMantainers().size() > 0).toList();
	}

	public void indexFiles() {
		for(int i = 0; i < files.size(); i++) {
			files.get(i).setId(i);
		}
	}
	
	public void executeHillClimbing() {
		int shotGunNumber = 100;
		int maxIterations = 100;
		List<Solution> listOfBestSolutions = new ArrayList<>();
		for(int i = 0; i < shotGunNumber; i++) {
			int[] solution = getInitialSolution();
			boolean flag = false;
			int j = 0;
			int[] backupSolution = new int[solution.length];
			while(flag == false && j < maxIterations) {
				backupSolution = Arrays.copyOf(solution, solution.length);
				solution = getBetterSolution(solution);
				if(solution == null) {
					flag = true;
				}
				j++;
			}
			listOfBestSolutions.add(new Solution(backupSolution, checkSolution(backupSolution)));
			i++;
		}
		printBestSolution(listOfBestSolutions);
		System.out.println();
	}
	
	private void printBestSolution(List<Solution> listOfBestSolutions) {
		Solution bestSolution = listOfBestSolutions.get(0);
		for(Solution solution: listOfBestSolutions) {
			if(solution.getFitness() < bestSolution.getFitness()) {
				bestSolution = solution;
			}
		}
		log.info("BEST SOLUTION: "+bestSolution.getFitness());
	}

	public int[] getBetterSolution(int[] currentSolution) {
		List<int[]> neighbors = new ArrayList<>();
		Random rd = new Random();
		for(int i = 0; i < currentSolution.length; i++) {
			boolean flag = false;
			while(flag == false) {
				Integer randomFileIndex =  rd.nextInt(files.size());
				if(arrayContains(currentSolution, randomFileIndex)) {
					continue;
				}else {
					flag = true;
				}
				int[] neighbor = Arrays.copyOf(currentSolution, currentSolution.length);
				neighbor[i] = randomFileIndex;
				neighbors.add(neighbor);
			}
		}
		int bestFit = checkSolution(currentSolution);
		int[] solution = null;
		for(int i = 0; i < neighbors.size(); i++) {
			int fitness = checkSolution(neighbors.get(i));
			if(fitness < bestFit) {
				bestFit = fitness;
				solution = neighbors.get(i);
			}
		}
		return solution;
	}
	
	public int[] getInitialSolution() {
		int total = files.size()+abandonedFiles.size();
		int halfPlusOne = (total/2)+1;
		int solutionLength = halfPlusOne - abandonedFiles.size();
		Random rd = new Random();
		int[] solution = new int[solutionLength];
		for (int i = 0; i < solutionLength; i++) {
			boolean flag = false;
			int randomFileIndex = 0;
			while(flag == false) {
				randomFileIndex =  rd.nextInt(files.size());
				if(arrayContains(solution, randomFileIndex) == false) {
					flag = true;
				}
			}
			solution[i] = randomFileIndex;
		}
		return solution;
	}
	
	private boolean arrayContains(int[] array, int i) {
		for (int j : array) {
			if(j == i) {
				return true;
			}
		}
		return false;
	}
	
	public int checkSolution(int[] abandonedFilesIds) {
		int j = 0;
		int[] contributors = new int[contributorsSize];
		for(int index: abandonedFilesIds) {
			File file = files.get(index);
			forMantainer:for (Contributor mantainer : file.getMantainers()) {
				if (arrayContains(contributors, mantainer.getId())) {
						continue forMantainer;
				}
				j++;
				for(int i = 0; i < contributors.length; i++) {
					if(contributors[i] == 0) {
						contributors[i] = mantainer.getId();
						break;
					}
				}
			}
		}
		return j;
	}
	
	public boolean checkIfTwoSolutionsEqual(Integer[] solution1, Integer[] solution2) {
		Arrays.sort(solution1);
		Arrays.sort(solution2);
		return solution1.equals(solution2);
	}
	
}
