package br.com.gitanalyzer.tasks;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class RemoveTempProjects {

	@Value("${configuration.clone.path}")
	private String tmpProjectsPath;

	@Scheduled(fixedRate = 1000*60*60*24)
	public void removeTemProjects() {
		System.out.println("Removing temp projects...");
		try {
			File directory = new File(tmpProjectsPath);
			org.apache.commons.io.FileUtils.deleteDirectory(directory);
			directory.mkdir();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
