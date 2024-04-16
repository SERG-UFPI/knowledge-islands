package br.com.gitanalyzer.exceptions;

public class LinkNotFoundOnCommitsException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public LinkNotFoundOnCommitsException(String link, String filePath) {
		super("Link "+link+" not found on commits of file "+filePath);
	}

}
