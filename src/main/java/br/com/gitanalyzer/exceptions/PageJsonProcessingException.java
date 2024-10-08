package br.com.gitanalyzer.exceptions;

public class PageJsonProcessingException extends Exception{

	private static final long serialVersionUID = 1L;
	public PageJsonProcessingException(String exception) {
		super(exception);
	}
}
