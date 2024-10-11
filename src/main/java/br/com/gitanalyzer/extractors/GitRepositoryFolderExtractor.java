package br.com.gitanalyzer.extractors;

import java.io.File;
import java.io.IOException;
import java.util.List;

import br.com.gitanalyzer.model.entity.GitRepositoryFolder;
import br.com.gitanalyzer.utils.Constants;

public class GitRepositoryFolderExtractor {

	public GitRepositoryFolder getGitRepositoryFolder(String path, String root, List<String> filesPaths) throws IOException {
		return buildGitRepositoryFolder(path, root, filesPaths);
	}

	public GitRepositoryFolder buildGitRepositoryFolder(String path, String root, List<String> filesPaths) throws IOException {
		File file = new File(path);
		if (!file.exists() || (!file.isDirectory() && !filesPaths.contains(file.getAbsolutePath()))) {
			return null;
		}
		String absolutePath = file.getAbsolutePath();
		if(file.isDirectory()) {
			absolutePath = absolutePath+Constants.getFileSeparator();
		}
		absolutePath = absolutePath.replace(root, "");
		GitRepositoryFolder node = new GitRepositoryFolder(file.getName(), absolutePath.isEmpty()?null:absolutePath);
		if (file.isDirectory()) {
			node.setFolder(true);
			File[] files = file.listFiles();
			if (files != null) {
				for (File child : files) {
					GitRepositoryFolder childNode = buildGitRepositoryFolder(child.getAbsolutePath(), root, filesPaths);
					if (childNode != null) {
						node.getChildren().add(childNode);
					}
				}
			}
			if(node.getChildren().isEmpty()) {
				return null;
			}
		}
		return node;
	}
}
