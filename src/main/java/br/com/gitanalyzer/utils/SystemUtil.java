package br.com.gitanalyzer.utils;

public class SystemUtil {

	public static int getNumberOfProcessors() {
		return Runtime.getRuntime().availableProcessors(); 
	}
}
