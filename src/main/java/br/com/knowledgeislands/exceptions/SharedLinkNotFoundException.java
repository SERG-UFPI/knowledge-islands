package br.com.knowledgeislands.exceptions;

public class SharedLinkNotFoundException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public SharedLinkNotFoundException(String link) {
		super("Shared link not found: "+link);
	}

}
