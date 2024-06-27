package br.com.gitanalyzer.exceptions;

public class CommitNotFoundBySha extends Exception{

	private static final long serialVersionUID = 1L;

	public CommitNotFoundBySha(String sha) {
		super("Commit not found by sha: "+sha);
	}

}