package br.com.gitanalyzer.utils;

public class SystemUtil {

	public static int getNumberOfProcessors() {
		return Runtime.getRuntime().availableProcessors(); 
	}
	
	public static String fixFolderPath(String path) {
		if(path.substring(path.length() -1).equals("/") == false) {
			path = path+"/";
		}
		return path;
	}
}
