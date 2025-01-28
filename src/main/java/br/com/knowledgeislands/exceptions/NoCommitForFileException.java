package br.com.knowledgeislands.exceptions;

public class NoCommitForFileException extends Exception{

	private static final long serialVersionUID = 1L;

	public NoCommitForFileException(String filePath) {
		super("No commit found for file: "+filePath);
	}

}