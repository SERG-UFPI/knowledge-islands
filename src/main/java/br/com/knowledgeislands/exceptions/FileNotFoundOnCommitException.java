package br.com.knowledgeislands.exceptions;

public class FileNotFoundOnCommitException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public FileNotFoundOnCommitException(String file, String commit) {
		super("File "+file+" not found on commit "+commit);
	}

}
