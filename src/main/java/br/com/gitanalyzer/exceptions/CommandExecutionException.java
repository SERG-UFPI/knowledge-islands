package br.com.gitanalyzer.exceptions;

public class CommandExecutionException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public CommandExecutionException(String message) {
		super(message);
	}

}
