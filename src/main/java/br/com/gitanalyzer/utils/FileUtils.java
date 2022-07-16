package br.com.gitanalyzer.utils;

public class FileUtils {

	public static String returnFileExtension(String path) {
		String extension = path.substring(path.lastIndexOf("/")+1);
		extension = extension.substring(extension.indexOf(".")+1);
		return extension;
	}

	public static String returnFileName(String path) {
		String name = path.substring(path.lastIndexOf("/")+1);
		name = name.substring(0, name.indexOf("."));
		return name;
	}

}