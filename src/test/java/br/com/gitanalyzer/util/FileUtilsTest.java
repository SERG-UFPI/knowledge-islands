package br.com.gitanalyzer.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import br.com.gitanalyzer.utils.FileUtils;

public class FileUtilsTest {

	@Test
	public void givenFilePath_returnFileName() {
		String filePath = "/path/to/the/fileName.txt";
		String fileName = "fileName";
		assertEquals(fileName, FileUtils.returnFileName(filePath));
	}

	@Test
	public void givenFilePath_returnExtension() {
		String filePath = "/path/to/the/fileName.txt";
		String extension = "txt";
		assertEquals(extension, FileUtils.returnFileExtension(filePath));
	}
}
