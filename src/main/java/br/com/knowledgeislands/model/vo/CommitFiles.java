package br.com.knowledgeislands.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CommitFiles implements Comparable<CommitFiles>{

	private String hash;
	private int numberOfFilesModified;

	public String[] toStringArray() {
		String[] toS = {hash, String.valueOf(numberOfFilesModified)};
		return toS;
	}

	@Override
	public int compareTo(CommitFiles o) {
		return this.numberOfFilesModified - o.numberOfFilesModified;
	}

}
