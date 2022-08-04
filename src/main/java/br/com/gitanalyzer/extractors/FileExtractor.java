package br.com.gitanalyzer.extractors;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;

import br.com.gitanalyzer.enums.OperationType;
import br.com.gitanalyzer.model.File;
import br.com.gitanalyzer.model.Project;
import br.com.gitanalyzer.utils.Constants;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class FileExtractor {

	public int extractSizeAllFiles(String path, String fileList) {
		int lines = 0;
		try {
			FileInputStream fstream = new FileInputStream(path+fileList);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if(!"".equals(strLine.trim())){
					lines++;
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	public List<File> extractFromFileList(String path, String fileListName, 
			String clocFileName, Repository repository, Project project){
		List<File> files = new ArrayList<File>();
		String fileListfullPath = path+fileListName;
		String clocListfullPath = path+clocFileName;
		if (fileListName.equals(Constants.linguistFileName)) {
			try {
				FileInputStream fstream = new FileInputStream(fileListfullPath);
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
				String strLine, strLineCloc;
				while ((strLine = br.readLine()) != null) {
					String filePath = strLine.split(";")[1];
					File file = new File(filePath);
					files.add(file);
				}
				br.close();
				if (clocFileName.equals(Constants.clocFileName)) {
					FileInputStream fstreamCloc = new FileInputStream(clocListfullPath);
					BufferedReader brCloc = new BufferedReader(new InputStreamReader(fstreamCloc));
					while ((strLineCloc = brCloc.readLine()) != null) {
						String filePathCloc = strLineCloc.split(";")[0];
						for (File file : files) {
							if (filePathCloc.equals(file.getPath())) {
								if (strLineCloc.split(";").length > 1) {
									String fileSizeString = strLineCloc.split(";")[2];
									if (fileSizeString != null && fileSizeString.equals("") == false) {
										file.setFileSize(Integer.parseInt(fileSizeString));
										break;
									}
								}
							}
						}
					}
					brCloc.close();
					for (File file : files) {
						if (file.getFileSize() == 0) {
							BlameCommand blameCommand = new BlameCommand(repository);
							blameCommand.setTextComparator(RawTextComparator.WS_IGNORE_ALL);
							blameCommand.setFilePath(file.getPath());
							BlameResult blameResult = blameCommand.call();
							RawText rawText = blameResult.getResultContents();
							file.setFileSize(rawText.size());
						}
					}
				}
			} catch (IOException | GitAPIException e) {
				e.printStackTrace();
			}
		}
		return files;
	}

	public void getRenamesFiles(String projectPath, List<File> files) {
		try {
			for (File file : files) {
				FileInputStream fstream = new FileInputStream(projectPath+Constants.commitFileFileName);
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
				String strLine;
				while ((strLine = br.readLine()) != null) {
					String[] splited = strLine.split(";");
					String operation = splited[1];
					if (operation.equals(OperationType.REN.getOperationType())) {
						String fileName = splited[3];
						String oldPath = splited[2];
						if (file.isFile(fileName)) {
							file.getRenamePaths().add(oldPath);
						}
					}
				}
				br.close();
			}
		}catch (Exception e) {
			log.error(e.getMessage());
		}
	}

}
