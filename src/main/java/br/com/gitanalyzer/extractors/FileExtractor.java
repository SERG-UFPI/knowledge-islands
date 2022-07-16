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

import br.com.gitanalyzer.model.File;
import br.com.gitanalyzer.model.Project;
import br.com.gitanalyzer.utils.Constants;
import br.com.gitanalyzer.utils.FileUtils;

public class FileExtractor {

	private Project project;

	public FileExtractor(Project project) {
		super();
		this.project = project;
	}

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

	public List<File> extractFromFileList(String path, String fileListName, String clocFileName, Repository repository){
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
					int fileSize = 0;
					if (clocFileName.equals(Constants.clocFileName)) {
						FileInputStream fstreamCloc = new FileInputStream(clocListfullPath);
						BufferedReader brCloc = new BufferedReader(new InputStreamReader(fstreamCloc));
						while ((strLineCloc = brCloc.readLine()) != null) {
							if (strLineCloc.split(";")[0].equals(filePath)) {
								if (strLineCloc.split(";").length > 1) {
									String fileSizeString = strLineCloc.split(";")[1];
									if (fileSizeString != null && fileSizeString.equals("") == false) {
										fileSize = Integer.parseInt(strLineCloc.split(";")[1]);
									}
								}
							}
						}
						brCloc.close();
						if (fileSize == 0) {
							BlameCommand blameCommand = new BlameCommand(repository);
							blameCommand.setTextComparator(RawTextComparator.WS_IGNORE_ALL);
							blameCommand.setFilePath(filePath);
							BlameResult blameResult = blameCommand.call();
							RawText rawText = blameResult.getResultContents();
							fileSize = rawText.size();
						}
					}
					File file = new File(filePath, project, 
							FileUtils.returnFileExtension(filePath), fileSize);
					files.add(file);
				}
				br.close();
			} catch (IOException | GitAPIException e) {
				e.printStackTrace();
			}
		}
		return files;
	}

}
