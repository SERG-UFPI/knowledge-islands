package br.com.gitanalyzer.service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CleanProjectService {

	@Value("${configuration.permanent-clone.path}")
	private String cloneFolder;
	@Value("${spring.jpa.hibernate.ddl-auto}")
	private String ddlAuto;

	public void cleanDowloadedProject() {
		Path directoryPath = Paths.get(cloneFolder);
		if(ddlAuto.equals("create")) {
			try {
				Files.walkFileTree(directoryPath, new SimpleFileVisitor<>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file); 
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						if (!dir.equals(directoryPath)) { 
							Files.delete(dir); 
						}
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
