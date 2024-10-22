package br.com.gitanalyzer.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.model.entity.File;
import br.com.gitanalyzer.model.entity.FileRepositorySharedLinkCommit;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.entity.GitRepositoryVersion;
import br.com.gitanalyzer.model.enums.OperationType;
import br.com.gitanalyzer.repository.FileRepositorySharedLinkCommitRepository;
import br.com.gitanalyzer.repository.GitRepositoryVersionRepository;
import br.com.gitanalyzer.utils.FileUtils;
import br.com.gitanalyzer.utils.KnowledgeIslandsUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class FileService {

	@Autowired
	private FileRepositorySharedLinkCommitRepository fileGitRepositorySharedLinkCommitRepository;
	@Autowired
	private GitRepositoryVersionRepository gitRepositoryVersionRepository;

	private String[] addProjectPatterns(GitRepository gitRepository) {
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
		return patterns;
	}

	public List<File> getFilesFromClocFile(GitRepository gitRepository) throws IOException {
		String[] patterns = addProjectPatterns(gitRepository);
		List<File> files = new ArrayList<>();
		List<File> filesWithoutSize = new ArrayList<>();
		List<File> filesRepository = new ArrayList<>();
		List<GitRepositoryVersion> versions = gitRepositoryVersionRepository.findByGitRepositoryId(gitRepository.getId());
		if(versions != null) {
			for (GitRepositoryVersion version : versions) {
				if(version.getFiles() != null) {
					filesRepository.addAll(version.getFiles());
				}
			}
		}
		List<FileRepositorySharedLinkCommit> filesSharedLinks = fileGitRepositorySharedLinkCommitRepository.findByGitRepositoryId(gitRepository.getId());
		if(filesSharedLinks != null && !filesSharedLinks.isEmpty()) {
			for(FileRepositorySharedLinkCommit fileSharedLink: filesSharedLinks) {
				if(filesRepository.stream().noneMatch(f -> f.getId().equals(fileSharedLink.getId()))) {
					filesRepository.add(fileSharedLink.getFile());
				}
			}
		}
		String clocListPath = gitRepository.getCurrentFolderPath()+KnowledgeIslandsUtils.clocFileName;
		java.io.File clocFile = new java.io.File(clocListPath);
		if(clocFile.exists() && clocFile.length() > 0) {
			try(BufferedReader brCloc = new BufferedReader(new InputStreamReader(new FileInputStream(clocListPath)));) {
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
							if(fileAux.getSize() != 0) {
								files.add(fileAux);
								continue whileFile;
							}
							file = fileAux;
							break;
						}
					}
					if (splitedLine.length == 3) {
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
			for (FileRepositorySharedLinkCommit fileRepositorySharedLinkCommit : filesSharedLinks) {
				if(files.stream().noneMatch(f -> f.isFile(fileRepositorySharedLinkCommit.getFile().getPath())) && 
						filesWithoutSize.stream().noneMatch(f -> f.isFile(fileRepositorySharedLinkCommit.getFile().getPath()))) {
					if(fileRepositorySharedLinkCommit.getFile().getSize() == 0) {
						filesWithoutSize.add(fileRepositorySharedLinkCommit.getFile());
					}else {
						files.add(fileRepositorySharedLinkCommit.getFile());
					}
				}
			}
		}else {
			List<String> extensions = FileUtils.getProgrammingExtensions();
			try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(gitRepository.getCurrentFolderPath()+KnowledgeIslandsUtils.allFilesFileName)));) {
				String strLine;
				whileFile:while ((strLine = br.readLine()) != null) {
					strLine = strLine.trim();
					if(!strLine.isBlank() && !strLine.isEmpty() && extensions.contains(FileUtils.getFileExtension(strLine).trim())){
						for (File fileAux : filesRepository) {
							if(fileAux.isFile(strLine)) {
								if(fileAux.getSize() == 0) {
									filesWithoutSize.add(new File(strLine));
								}else {
									files.add(fileAux);
								}
								continue whileFile;
							}
						}
						filesWithoutSize.add(new File(strLine));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!filesWithoutSize.isEmpty()) {
			for (File file : filesWithoutSize) {
				if (file.getSize() == 0) {
					try(BufferedReader reader = new BufferedReader(new FileReader(gitRepository.getCurrentFolderPath()+file.getPath()));){
						int lines = 0;
						while (reader.readLine() != null) lines++;
						if(lines > 0) {
							file.setSize(lines);
							files.add(file);
						}
					}
				}

			}
		}
		return files;
	}

	public void getRenamesFiles(String projectPath, List<File> files) throws IOException {
		HashMap<String, String> newOldName = new HashMap<String, String>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(projectPath+KnowledgeIslandsUtils.commitFileFileName)));){
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] splited = strLine.split(";");
				if (splited.length >= 4) {
					String operation = splited[1];
					if (operation.equals(OperationType.RENAMED.name())) {
						String oldPath = splited[2];
						String fileName = splited[3];
						newOldName.put(oldPath, fileName);
					}
				}
			}
			for (File file : files) {
				if(file.getRenamePaths() == null || file.getRenamePaths().isEmpty()) {
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
		}
	}
}
