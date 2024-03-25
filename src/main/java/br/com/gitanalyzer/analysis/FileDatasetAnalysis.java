package br.com.gitanalyzer.analysis;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gitanalyzer.utils.FileUtils;

public class FileDatasetAnalysis {

	public static void main(String[] args) throws IOException, URISyntaxException {
		ObjectMapper objectMapper = new ObjectMapper();
		
		List<String> extensions = new ArrayList<>();
		String languagesExtensions = "jsons/programming_languages_extensions.json";
		ClassLoader classLoader = FileDatasetAnalysis.class.getClassLoader();
		URL resourceURL = classLoader.getResource(languagesExtensions);
		JsonNode arrayLanguage = objectMapper.readTree(new File(resourceURL.toURI()));
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
		
		List<String> typesOfFiles = new ArrayList<>();
		List<String> reposNames = new ArrayList<>();
		List<String> filesUrl = new ArrayList<>();
		List<String> filesName = new ArrayList<>();
		List<String> reposLanguages = new ArrayList<>();
		String filePath = "/home/otavio/devgpt_file.json";
		JsonNode rootNode = objectMapper.readTree(new File(filePath));
		if(rootNode.has("Sources") && rootNode.get("Sources").isArray()) {
			JsonNode sourcesArrays = rootNode.get("Sources");
			for (JsonNode sourceNode : sourcesArrays) {
				String type = sourceNode.get("Type").asText();
				String fileName = sourceNode.get("FileName").asText();
				filesName.add(fileName);
				String extension = FileUtils.getFileExtension(fileName);
				if(typesOfFiles.contains(extension) == false) {
					typesOfFiles.add(extension);
				}
				String repoName = sourceNode.get("RepoName").asText();
				if(reposNames.contains(repoName) == false) {
					reposNames.add(repoName);
				}
				String fileUrl = sourceNode.get("URL").asText();
				if(filesUrl.contains(fileUrl) == false) {
					filesUrl.add(fileUrl);
				}
				String repoLanguage = sourceNode.get("RepoLanguage").asText();
				if(reposLanguages.contains(repoLanguage) == false) {
					reposLanguages.add(repoLanguage);
				}
			}
		}
		System.out.println("======== Types of files ========");
		typesOfFiles = typesOfFiles.stream().sorted().toList();
		typesOfFiles.stream().forEach((f)->{System.out.println(f);});
		System.out.println("======== Repositories languages ========");
		reposLanguages = reposLanguages.stream().sorted().toList();
		reposLanguages.stream().forEach((f)->{System.out.println(f);});
		System.out.println("Number of types of files: "+typesOfFiles.size());

		reposNames = reposNames.stream().distinct().toList();
		System.out.println("Number of repositories: "+reposNames.size());

		filesUrl = filesUrl.stream().distinct().toList();
		System.out.println("Number of files: "+filesUrl.size());

		reposLanguages = reposLanguages.stream().distinct().toList();
		System.out.println("Number of repos languages: "+reposLanguages.size());
		
		List<String> filesProgramming = new ArrayList<>();
		for (String fileName : filesName) {
			String extension = FileUtils.getFileExtension(fileName);
			if(extensions.contains(extension)) {
				filesProgramming.add(fileName);
			}
		}
		System.out.println("Number of files of programming: "+filesProgramming.size());
	}
}
