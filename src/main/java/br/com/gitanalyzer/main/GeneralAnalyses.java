package br.com.gitanalyzer.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;


public class GeneralAnalyses {

	public static void main(String[] args) {
		File dir = new File(args[0]);
		List<String[]> r = null;
		try (CSVReader reader = new CSVReader(new FileReader(args[0]+"inputs_reps.csv"))) {
			r = reader.readAll();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CsvException e) {
			e.printStackTrace();
		}
		List<String> projetosArquivo = new ArrayList<String>();
		for (String[] string : r) {
			String url = string[0];
			String[] urlSplited = url.split("/");
			projetosArquivo.add(urlSplited[urlSplited.length-1]);
		}
		List<String> projetosDirectory = new ArrayList<String>();
		for (File fileDir: dir.listFiles()) {
			if (fileDir.isDirectory()) {
				String[] projectPathSplited = fileDir.getAbsolutePath().split("/");
				String name = projectPathSplited[projectPathSplited.length-1];
				projetosDirectory.add(name);
			}
		}
		for (String string : projetosArquivo) {
			if (projetosDirectory.contains(string) == false) {
				System.out.println(string);
			}
		}
		System.out.println();
	}

}
