package br.com.gitanalyzer.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileUtils {

	public static List<String> getProgrammingLanguagesAliasGithub(){
		List<String> alias = new ArrayList<>();
		alias.add("python");
		alias.add("javascript");
		alias.add("java");
		alias.add("typescript");
//		alias.add("csharp");
//		alias.add("cpp");
//		alias.add("c");
//		alias.add("php");
//		alias.add("ruby");
//		alias.add("go");
//		alias.add("shell");
//		alias.add("rust");
//		alias.add("kotlin");
//		alias.add("r");
//		alias.add("swift");
//		alias.add("lua");
		return alias;
	}

	public static String getFileExtension(String path) {
		if(path.contains("/")) {
			String extension = path.substring(path.lastIndexOf("/")+1);
			extension = extension.substring(extension.indexOf(".")+1);
			return extension;
		}else {
			String extension = path.substring(path.indexOf(".")+1);
			return extension;
		}
	}

	public static String getFileName(String path) {
		String name = path.substring(path.lastIndexOf("/")+1);
		name = name.substring(0, name.indexOf("."));
		return name;
	}

	public static List<String> getProgrammingExtensions(){
		ObjectMapper objectMapper = new ObjectMapper();
		List<String> extensions = new ArrayList<>();
		String languagesExtensions = "jsons/programming_languages_extensions.json";
		ClassLoader classLoader = FileUtils.class.getClassLoader();
		URL resourceURL = classLoader.getResource(languagesExtensions);
		JsonNode arrayLanguage = null;
		try {
			arrayLanguage = objectMapper.readTree(new File(resourceURL.toURI()));
			for(JsonNode languageNode: arrayLanguage) {
				if(languageNode.get("type").asText().equals("programming")) {
					JsonNode arrayExtensions = languageNode.get("extensions");
					if(arrayExtensions != null) {
						for (JsonNode extensionNode : arrayExtensions) {
							extensions.add(extensionNode.asText());
						}
					}
				}
			}
			extensions = extensions.stream().map(e -> e.replace(".", "")).toList();
			return extensions;
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

}