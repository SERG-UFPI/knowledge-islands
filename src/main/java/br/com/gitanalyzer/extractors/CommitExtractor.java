package br.com.gitanalyzer.extractors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jgit.api.Git;
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

import br.com.gitanalyzer.enums.OperationType;
import br.com.gitanalyzer.model.Commit;
import br.com.gitanalyzer.model.CommitFile;
import br.com.gitanalyzer.model.Contributor;
import br.com.gitanalyzer.model.File;
import br.com.gitanalyzer.utils.Constants;

public class CommitExtractor {

	/**
	 * Extract commits without persistence
	 * @param files
	 * @param git
	 * @param repository
	 * @return
	 * @throws NoHeadException
	 * @throws GitAPIException
	 * @throws AmbiguousObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	public List<Commit> extractCommitsWithoutPersistence(List<File> files, Git git, Repository repository) throws NoHeadException, 
	GitAPIException, AmbiguousObjectException, IncorrectObjectTypeException, IOException{
		HashMap<String, List<String>> arquivoRenames = new HashMap<String, List<String>>();
		for (File file : files) {
			arquivoRenames.put(file.getPath(), new ArrayList<String>());
		}

		boolean analyse;
		List<Commit> commits = new ArrayList<Commit>();

		Iterable<RevCommit> commitsIterable = git.log().setRevFilter(RevFilter.NO_MERGES).call();
		List<RevCommit> commitsList = new ArrayList<RevCommit>();
		commitsIterable.forEach(commitsList::add);
		Collections.sort(commitsList, new Comparator<RevCommit>() {
			public int compare(RevCommit commit1, RevCommit commit2) {
				if (commit1.getAuthorIdent().getWhen().after(commit2.getAuthorIdent().getWhen())) {
					return -1;
				}else if(commit1.getAuthorIdent().getWhen().before(commit2.getAuthorIdent().getWhen())) {
					return 1;
				}else {
					return 0;
				}
			}
		});
		for (RevCommit jgitCommit: commitsList) {
			String nome = null, email = null;
			if (jgitCommit.getAuthorIdent() != null) {
				if (jgitCommit.getAuthorIdent().getEmailAddress() != null) {
					email = jgitCommit.getAuthorIdent().getEmailAddress();
				}else {
					email = jgitCommit.getCommitterIdent().getEmailAddress();
				}
				if (jgitCommit.getAuthorIdent().getName() != null) {
					nome = jgitCommit.getAuthorIdent().getName();
				}else {
					nome = jgitCommit.getCommitterIdent().getName();
				}
			}else {
				email = jgitCommit.getCommitterIdent().getEmailAddress();
				nome = jgitCommit.getCommitterIdent().getName();
			}
			List<DiffEntry> diffsForTheCommit = diffsForTheCommit(repository, jgitCommit);
			analyse = false;
			for (DiffEntry diff : diffsForTheCommit) {
				String newPath = diff.getNewPath();
				String oldPath = diff.getOldPath();
				Iterator<Entry<String, List<String>>> it = arquivoRenames.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, List<String>> pair = (Map.Entry<String, List<String>>) it.next();
					if(pair.getKey().equals(newPath) || pair.getValue().contains(newPath)
							|| pair.getKey().equals(oldPath) || pair.getValue().contains(oldPath)) {
						analyse = true;
						if (diff.getChangeType().name().equals(Constants.RENAME) && 
								pair.getValue().contains(oldPath) == false) {
							pair.getValue().add(oldPath);
						}
					}
				}
			}
			if (analyse) {
				Contributor author = new Contributor(nome, email);
				Commit commit = new Commit();
				commit.setExternalId(jgitCommit.getName());
				commit.setAuthor(author);
				commit.setDate(jgitCommit.getAuthorIdent().getWhen());
				commit.setCommitFiles(new ArrayList<CommitFile>());
				for (DiffEntry diff : diffsForTheCommit) {
					String newPath = diff.getNewPath();
					String oldPath = diff.getOldPath();
					Iterator<Entry<String, List<String>>> it = arquivoRenames.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<String, List<String>> pair = (Map.Entry<String, List<String>>) it.next();
						if(pair.getKey().equals(newPath) || pair.getValue().contains(newPath)
								|| pair.getKey().equals(oldPath) || pair.getValue().contains(oldPath)) {
							CommitFile commitFile = new CommitFile();
							if(diff.getChangeType().name().equals(Constants.ADD)){
								commitFile.setOperation(OperationType.ADD);
							}else if(diff.getChangeType().name().equals(Constants.DELETE)){
								commitFile.setOperation(OperationType.DEL);
							}else if(diff.getChangeType().name().equals(Constants.MODIFY)){
								commitFile.setOperation(OperationType.MOD);
							}else if(diff.getChangeType().name().equals(Constants.RENAME)) {
								commitFile.setOperation(OperationType.REN);
							}else{
								continue;
							}

							ByteArrayOutputStream stream = new ByteArrayOutputStream();
							DiffFormatter diffFormatter = new DiffFormatter( stream );
							diffFormatter.setRepository(repository);
							diffFormatter.format(diff);

							String in = stream.toString();

							Map<String, Integer> modifications = analyze(in);
							commitFile.setAdds(modifications.get("adds"));
							for (File file: files) {
								if (file.getPath().equals(pair.getKey())) {
									commitFile.setFile(file);
								}
							}
							commit.getCommitFiles().add(commitFile);

							diffFormatter.flush();
							diffFormatter.close();		        
						}
					}
				}
				commits.add(commit);
			}
		}
		return commits;
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

	private Map<String, Integer> analyze(String fileDiff){
		int adds = 0;
		HashMap<String, Integer> modifications = new HashMap<String, Integer>();
		if(fileDiff !=null ){
			String[] lines = fileDiff.split("\n");

			for(int i = 0; i < lines.length; i++){
				if((i > 3) && (lines[i].length() > 0)){
					if((lines[i].charAt(0) == '+') && (lines[i].substring(1).trim().length() > 0)) {
						adds++;
					}
				}
			}
		}
		modifications.put("adds", adds);
		return modifications;
	}

}
