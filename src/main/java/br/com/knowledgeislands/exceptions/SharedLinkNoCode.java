package br.com.knowledgeislands.exceptions;

public class SharedLinkNoCode extends Exception{

	private static final long serialVersionUID = 1L;
	
	public SharedLinkNoCode(String link) {
		super("Shared link with no code: "+link);
	}

}
