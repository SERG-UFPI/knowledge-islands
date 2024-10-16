package br.com.gitanalyzer.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.FileGitRepositorySharedLinkCommit;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.entity.GitRepositoryVersion;
import br.com.gitanalyzer.model.enums.OperationType;
import br.com.gitanalyzer.repository.FileGitRepositorySharedLinkCommitRepository;
import br.com.gitanalyzer.repository.GitRepositoryVersionRepository;
import br.com.gitanalyzer.utils.KnowledgeIslandsUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class FileService {

	@Autowired
	private FileGitRepositorySharedLinkCommitRepository fileGitRepositorySharedLinkCommitRepository;
	@Autowired
	private GitRepositoryVersionRepository gitRepositoryVersionRepository;

	public List<File> getFilesFromClocFile(GitRepository gitRepository) throws IOException {
		Map<String, String[]> projectPatterns  = new HashMap<>();
		String[] arrayLinux = new String[] {"drivers/", "crypto/", "sound/", "security/"};
		String[] arrayHomebrew = new String[] {"Library/Formula/"};
		String[] arrayHomebrewCask = new String[] {"Casks/"};
		projectPatterns.put("linux", arrayLinux);
		projectPatterns.put("homebrew", arrayHomebrew);
		projectPatterns.put("homebrew-cask", arrayHomebrewCask);
		String[] patterns = null;
		if (projectPatterns.containsKey(gitRepository.getName())) {
			patterns = projectPatterns.get(gitRepository.getName());
		}
		List<File> files = new ArrayList<>();
		List<File> filesWithoutSize = new ArrayList<>();
		String clocListPath = gitRepository.getCurrentFolderPath()+KnowledgeIslandsUtils.clocFileName;
		List<File> filesRepository = new ArrayList<>();
		List<GitRepositoryVersion> versions = gitRepositoryVersionRepository.findByGitRepositoryId(gitRepository.getId());
		if(versions != null) {
			for (GitRepositoryVersion version : versions) {
				if(version.getFiles() != null) {
					filesRepository.addAll(version.getFiles());
				}
			}
		}
		List<FileGitRepositorySharedLinkCommit> filesSharedLinks = fileGitRepositorySharedLinkCommitRepository.findByGitRepositoryId(gitRepository.getId());
		if(filesSharedLinks != null && !filesSharedLinks.isEmpty()) {
			for(FileGitRepositorySharedLinkCommit fileSharedLink: filesSharedLinks) {
				if(filesRepository.stream().noneMatch(f -> f.getId().equals(fileSharedLink.getId()))) {
					filesRepository.add(fileSharedLink.getFile());
				}
			}
		}
		FileInputStream fstreamCloc = new FileInputStream(clocListPath);
		try(BufferedReader brCloc = new BufferedReader(new InputStreamReader(fstreamCloc));) {
			String strLineCloc;
			whileFile: while ((strLineCloc = brCloc.readLine()) != null) {
				String[] splitedLine = strLineCloc.split(";");
				String filePath = splitedLine[0];
				if (patterns != null) {
					for (String startPattern : patterns) {
						if (filePath.startsWith(startPattern)) {
							continue whileFile;
						}
					}
				}
				File file = null;
				for (File fileAux : filesRepository) {
					if(fileAux.isFile(filePath)) {
						file = fileAux;
						break;
					}
				}
				if(file != null && file.getSize() != 0) {
					files.add(file);
				}else if (splitedLine.length == 3) {
					String fileSizeString = splitedLine[2];
					if (fileSizeString != null && !fileSizeString.equals("")) {
						if(!fileSizeString.equals("0")) {
							int size = Integer.parseInt(fileSizeString);
							if(file == null) {
								file = new File(filePath, size);
							}else {
								file.setSize(size);
							}
							files.add(file);
						}
					}else {
						filesWithoutSize.add(file == null ? new File(filePath):file);
					}
				}else {
					filesWithoutSize.add(file == null ? new File(filePath):file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(!filesWithoutSize.isEmpty()) {
			Repository repository = null;
			try(Git git = Git.open(new java.io.File(gitRepository.getCurrentFolderPath()));) {
				repository = git.getRepository();
				for (File file : filesWithoutSize) {
					if (file.getSize() == 0) {
						BlameCommand blameCommand = new BlameCommand(repository);
						blameCommand.setTextComparator(RawTextComparator.WS_IGNORE_ALL);
						blameCommand.setFilePath(file.getPath());
						BlameResult blameResult = blameCommand.call();
						if(blameResult != null) {
							RawText rawText = blameResult.getResultContents();
							file.setSize(rawText.size());
							if(file.getSize() > 0) {
								files.add(file);
							}
						}
					}
				}
			} catch (IOException | GitAPIException e1) {
				e1.printStackTrace();
				log.error(e1.getMessage());
			}
		}
		return files;
	}

	public void getRenamesFiles(String projectPath, List<File> files) throws IOException {
		HashMap<String, String> newOldName = new HashMap<String, String>();
		BufferedReader br = null;
		try {
			FileInputStream fstream = new FileInputStream(projectPath+KnowledgeIslandsUtils.commitFileFileName);
			br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] splited = strLine.split(";");
				String operation = splited[1];
				if (operation.equals(OperationType.RENAMED.name())) {
					String oldPath = splited[2];
					String fileName = splited[3];
					newOldName.put(oldPath, fileName);
				}
			}
			for (File file : files) {
				if(file.getId() == null) {
					for (Map.Entry<String, String> entry : newOldName.entrySet()) {
						String fileName = entry.getValue();
						if (file.isFile(fileName)) {
							file.getRenamePaths().add(entry.getKey());
						}
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			br.close();
		}
	}
}
