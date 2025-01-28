package br.com.knowledgeislands.exceptions;

public class FetchPageException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public FetchPageException(String link) {
		super("Error fetching link: "+link);
	}

}
