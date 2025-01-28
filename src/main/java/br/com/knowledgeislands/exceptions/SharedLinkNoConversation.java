package br.com.knowledgeislands.exceptions;

public class SharedLinkNoConversation extends Exception{

	private static final long serialVersionUID = 1L;
	
	public SharedLinkNoConversation(String link) {
		super("Shared link with not conversation: "+link);
	}

}
