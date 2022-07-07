package br.com.gitanalyzer.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;


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

	public List<String> currentFiles(Repository repository) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		Ref head = repository.exactRef("HEAD");
		List<String> filesPath = new ArrayList<String>();
		RevWalk walk = new RevWalk(repository);
		RevCommit commit = walk.parseCommit(head.getObjectId());
		RevTree tree = commit.getTree();
		TreeWalk treeWalk = new TreeWalk(repository);
		treeWalk.addTree(tree);
		treeWalk.setRecursive(true);
		while (treeWalk.next()) {
			filesPath.add(treeWalk.getPathString());
		}
		treeWalk.close();
		walk.close();
		return filesPath;
	}


}