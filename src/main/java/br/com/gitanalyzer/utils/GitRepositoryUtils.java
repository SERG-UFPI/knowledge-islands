package br.com.gitanalyzer.utils; 

public class GitRepositoryUtils {
	
	public static String getOwnerNameProject(String fullName) {
		return fullName.split("/")[0];
	}
	
	public static String getNameProject(String fullName) {
		return fullName.split("/")[1];
	}
}