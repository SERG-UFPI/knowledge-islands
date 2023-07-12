package br.com.gitanalyzer.combinatory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.com.gitanalyzer.model.entity.File;
import lombok.Data;

@Data
public class CombinatoryProblem {

	protected List<File> files;
	protected List<File> abandonedFiles;
	protected int contributorsSize;
	protected int solutionLength;

	public void setProblem() {
		removeAbandonedFiles();
		indexFiles();
		setSolutionLength();
	}

	protected void setSolutionLength() {
		int total = files.size()+abandonedFiles.size();
		int halfPlusOne = (total/2)+1;
		solutionLength = halfPlusOne - abandonedFiles.size();
	}

	protected void removeAbandonedFiles() {
		abandonedFiles = files.stream().filter(f -> f.getMaintainers() == null || f.getMaintainers().size() == 0).toList();
		if(abandonedFiles == null) {
			abandonedFiles = new ArrayList<>();
		}
		files = files.stream().filter(f -> f.getMaintainers() != null && f.getMaintainers().size() > 0).toList();
	}

	protected void indexFiles() {
		//		for(int i = 0; i < files.size(); i++) {
		//			files.get(i).setId(i);
		//		}
	}

	protected List<Integer> getFilesIndexes(){
		//		return files.stream().map(f -> f.getId()).toList();
		return null;
	}

	//	protected int checkSolution(int[] abandonedFilesIds) {
	//		int j = 0;
	//		int[] contributors = new int[contributorsSize];
	//		for(int index: abandonedFilesIds) {
	//			File file = files.get(index);
	//			forMantainer:for (Contributor mantainer : file.getMantainers()) {
	//				if (arrayContains(contributors, mantainer.getId())) {
	//						continue forMantainer;
	//				}
	//				j++;
	//				for(int i = 0; i < contributors.length; i++) {
	//					if(contributors[i] == 0) {
	//						contributors[i] = mantainer.getId();
	//						break;
	//					}
	//				}
	//			}
	//		}
	//		return j;
	//	}

	protected int[] getSolution() {
		Random rd = new Random();
		int[] solution = new int[solutionLength];
		for (int i = 0; i < solutionLength; i++) {
			int randomFileIndex = 0;
			while(true) {
				randomFileIndex =  rd.nextInt(files.size());
				if(arrayContains(solution, randomFileIndex) == false) {
					break;
				}
			}
			solution[i] = randomFileIndex;
		}
		return solution;
	}

	protected boolean arrayContains(int[] array, int i) {
		for (int j : array) {
			if(j == i) {
				return true;
			}
		}
		return false;
	}

}
