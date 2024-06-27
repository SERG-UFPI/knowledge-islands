package br.com.gitanalyzer.exceptions;

public class NoCommitForRepositoryException extends Exception{

	private static final long serialVersionUID = 1L;

	public NoCommitForRepositoryException(String repositoryName) {
		super("No commit found for repository: "+repositoryName);
	}

}