package br.com.knowledgeislands.service;

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

import br.com.knowledgeislands.model.entity.File;
import br.com.knowledgeislands.model.entity.FileRepositorySharedLinkCommit;
import br.com.knowledgeislands.model.entity.GitRepository;
import br.com.knowledgeislands.model.entity.GitRepositoryVersion;
import br.com.knowledgeislands.model.enums.OperationType;
import br.com.knowledgeislands.repository.FileRepository;
import br.com.knowledgeislands.repository.FileRepositorySharedLinkCommitRepository;
import br.com.knowledgeislands.utils.FileUtils;
import br.com.knowledgeislands.utils.KnowledgeIslandsUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class FileService {

	@Autowired
	private FileRepositorySharedLinkCommitRepository fileGitRepositorySharedLinkCommitRepository;
	@Autowired
	private FileRepository repository;

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

	private List<File> addFilesRepositoryVersions(GitRepository gitRepository) {
		List<File> filesRepository = new ArrayList<>();
		if(gitRepository.getGitRepositoryVersion() != null) {
			for (GitRepositoryVersion version : gitRepository.getGitRepositoryVersion()) {
				if(version.getFiles() != null) {
					for (File file : version.getFiles()) {
						if(filesRepository.stream().anyMatch(f -> f.getPath().equals(file.getPath()))) {
							continue;
						}
						filesRepository.addAll(version.getFiles());
					}
				}
			}
		}
		return filesRepository;
	}

	public List<File> getFilesFromClocFile(GitRepository gitRepository) throws IOException {
		String[] patterns = addProjectPatterns(gitRepository);
		List<File> files = new ArrayList<>();
		List<File> filesWithoutSize = new ArrayList<>();
		List<File> filesRepository = addFilesRepositoryVersions(gitRepository);
		List<FileRepositorySharedLinkCommit> filesSharedLinks = fileGitRepositorySharedLinkCommitRepository.findByGitRepositoryId(gitRepository.getId());
		if(filesSharedLinks != null && !filesSharedLinks.isEmpty()) {
			for(FileRepositorySharedLinkCommit fileSharedLink: filesSharedLinks) {
				if(filesRepository.stream().anyMatch(f -> f.getId().equals(fileSharedLink.getId()))) {
					continue;
				}
				filesRepository.add(fileSharedLink.getFile());
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
					if(KnowledgeIslandsUtils.containsNonAscii(filePath)) {
						filePath = KnowledgeIslandsUtils.encodeNonAsciiOnly(filePath);
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
					boolean addFilesWithoutSize = true;
					if (splitedLine.length == 3) {
						String fileSizeString = splitedLine[2];
						if (fileSizeString != null && !fileSizeString.equals("") && !fileSizeString.equals("0")) {
							addFilesWithoutSize = false;
							int size = Integer.parseInt(fileSizeString);
							if(file == null) {
								file = new File(filePath, size);
							}else {
								file.setSize(size);
							}
							files.add(file);
						}
					}
					if(addFilesWithoutSize) {
						filesWithoutSize.add(file == null ? new File(filePath):file);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				log.error(e.getMessage());
			}
			for (FileRepositorySharedLinkCommit fileRepositorySharedLinkCommit : filesSharedLinks) {
				String fileLinkPath = fileRepositorySharedLinkCommit.getFile().getPath();
				if(filesWithoutSize.stream().anyMatch(f -> f.isFile(fileLinkPath)) || files.stream().anyMatch(f -> f.isFile(fileLinkPath))) {
					continue;
				}
				if(fileRepositorySharedLinkCommit.getFile().getSize() == 0) {
					filesWithoutSize.add(fileRepositorySharedLinkCommit.getFile());
				}else {
					files.add(fileRepositorySharedLinkCommit.getFile());
				}
			}
		}else {
			List<String> extensions = FileUtils.getProgrammingExtensions();
			try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(gitRepository.getCurrentFolderPath()+KnowledgeIslandsUtils.allFilesFileName)));) {
				String filePath;
				whileFile:while ((filePath = br.readLine()) != null) {
					filePath = KnowledgeIslandsUtils.removeEnclosingQuotes(filePath.trim());
					if(!filePath.isBlank() && !filePath.isEmpty() && extensions.contains(FileUtils.getFileExtension(filePath).trim())){
						for (File fileAux : filesRepository) {
							if(fileAux.isFile(filePath)) {
								if(fileAux.getSize() == 0) {
									filesWithoutSize.add(new File(filePath));
								}else {
									files.add(fileAux);
								}
								continue whileFile;
							}
						}
						filesWithoutSize.add(new File(filePath));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				log.error(e.getMessage());
			}
		}
		if(!filesWithoutSize.isEmpty()) {
			for (File file : filesWithoutSize) {
				if(KnowledgeIslandsUtils.containsOctalEncoding(file.getPath())) {
					file.setPath(KnowledgeIslandsUtils.decodeOctalString(file.getPath()));
				}
				try(BufferedReader reader = new BufferedReader(new FileReader(gitRepository.getCurrentFolderPath()+file.getPath()));){
					int lines = 0;
					while (reader.readLine() != null) lines++;
					if(lines > 0) {
						file.setSize(lines);
						files.add(file);
					}
				}catch(Exception e) {
					e.printStackTrace();
					log.error(e.getMessage());
				}
			}
		}
		fixNonAsciiFilePaths(files);
		return files;
	}

	public void getRenamesFiles(String projectPath, List<File> files) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(projectPath+KnowledgeIslandsUtils.commitFileFileName)));){
			List<FileCommitContent> content = new ArrayList<>();
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] splited = strLine.split(";");
				if (splited.length >= 4) {
					String operation = splited[1].trim();
					if (operation.equals(OperationType.RENAMED.name())) {
						String oldPath = KnowledgeIslandsUtils.removeEnclosingQuotes(splited[2].trim());
						String fileName = KnowledgeIslandsUtils.removeEnclosingQuotes(splited[3].trim());
						content.add(new FileCommitContent(oldPath, fileName));
					}
				}
			}
			for (File file : files) {
				for (FileCommitContent entry : content) {
					if(file.isFile(entry.fileName) && file.getRenamePaths().stream().noneMatch(p -> p.equals(entry.oldPath))) {
						file.getRenamePaths().add(entry.oldPath);
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}

	private class FileCommitContent {
		String oldPath;
		String fileName;
		public FileCommitContent(String oldPath, String fileName) {
			this.oldPath = oldPath;
			this.fileName = fileName;
		}
	}

	public void fixChinesePaths() {
		List<File> files = repository.findAll();
		fixNonAsciiFilePaths(files);
		repository.saveAll(files);
	}

	private void fixNonAsciiFilePaths(List<File> files) {
		for (File file : files) {
			if(KnowledgeIslandsUtils.containsNonAscii(file.getPath())) {
				file.setPath(KnowledgeIslandsUtils.encodeNonAsciiOnly(file.getPath()));
			}
		}
	}
}
