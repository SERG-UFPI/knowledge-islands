package br.com.gitanalyzer.extractors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import br.com.gitanalyzer.model.entity.SharedLink;
import br.com.gitanalyzer.model.github_openai.FileLinkAuthor;

public class CommitFileExtractor {

	public void getCommitFileFromFile(SharedLink sharedLink) throws IOException, NoHeadException, GitAPIException {
		Git git = Git.open(new java.io.File(sharedLink.getRepository().getCurrentGitFolderPath()));
		Repository repository = git.getRepository();
		for (FileLinkAuthor fileLinkAuthor : sharedLink.getFilesLinkAuthor()) {
			LogCommand logCommand = git.log().setRevFilter(RevFilter.NO_MERGES);
			logCommand.addPath(fileLinkAuthor.getAuthorFile().getFileVersion().getFile().getPath());
			Iterable<RevCommit> commitsIterable = logCommand.call();
			List<RevCommit> commits = new ArrayList<>();
			for (RevCommit commit : commitsIterable) {
				commits.add(commit);
				List<DiffEntry> diffsForTheCommit = diffsForTheCommit(repository, commit);
				for (DiffEntry diff : diffsForTheCommit) {
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					DiffFormatter diffFormatter = new DiffFormatter( stream );
					diffFormatter.setRepository(repository);
					diffFormatter.format(diff);
					String in = stream.toString();
					List<String> linesAdded = analyze(in);
					System.out.println();
					diffFormatter.flush();
					diffFormatter.close();
				}
			}
		}
		System.out.println();
	}

	private List<DiffEntry> diffsForTheCommit(Repository repo, RevCommit commit) throws IOException, AmbiguousObjectException, 
	IncorrectObjectTypeException { 
		AnyObjectId currentCommit = repo.resolve(commit.getName()); 
		AnyObjectId parentCommit = commit.getParentCount() > 0 ? repo.resolve(commit.getParent(0).getName()) : null; 
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE); 
		df.setBinaryFileThreshold(2 * 1024); //2 MB MAX A FILE
		df.setRepository(repo); 
		df.setDiffComparator(RawTextComparator.DEFAULT); 
		df.setDetectRenames(true); 
		List<DiffEntry> diffs = null; 
		if (parentCommit == null) { 
			RevWalk rw = new RevWalk(repo); 
			diffs = df.scan(new EmptyTreeIterator(), new CanonicalTreeParser(null, rw.getObjectReader(), commit.getTree())); 
			rw.close(); 
		} else { 
			diffs = df.scan(parentCommit, currentCommit); 
		} 
		df.close();
		return diffs; 
	}

	private List<String> analyze(String fileDiff){
		List<String> addedLines = new ArrayList<>();
		if(fileDiff !=null ){
			String[] lines = fileDiff.split("\n");
			for(int i = 0; i < lines.length; i++){
				if((lines[i].length() > 0) && (lines[i].charAt(0) == '+') && (lines[i].substring(1).trim().length() > 0)) {
					addedLines.add(lines[i]);				
				}
			}
		}
		return addedLines;
	}
}
