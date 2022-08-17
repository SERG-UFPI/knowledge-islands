package br.com.gitanalyzer.main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;


public class GeneralAnalyses {

	public static void main(String[] args) {
		File dir = new File(args[0]);
		List<String[]> doaFile = null;
		try (CSVReader reader = new CSVReader(new FileReader(args[0]+"reps_doa.csv"))) {
			doaFile = reader.readAll();
		} catch (IOException | CsvException e) {
			e.printStackTrace();
		}
		List<String[]> doeFile = null;
		try (CSVReader reader = new CSVReader(new FileReader(args[0]+"reps_doe.csv"))) {
			doeFile = reader.readAll();
		} catch (IOException | CsvException e) {
			e.printStackTrace();
		}
		HashMap<String, Integer> projectDoa = new HashMap<String, Integer>();
		for (String[] string : doaFile) {
			String url = string[0];
			String[] urlSplited = url.split("/");
			projectDoa.put(urlSplited[urlSplited.length-1], Integer.parseInt(string[1]));
		}
		HashMap<String, Integer> projectDoe = new HashMap<String, Integer>();
		for (String[] string : doeFile) {
			projectDoe.put(string[0], Integer.parseInt(string[1]));
		}
		List<ProjectDoaDoe> projects = new ArrayList<ProjectDoaDoe>();
		for (Map.Entry<String, Integer> entryDoe : projectDoe.entrySet()) {
			String projectNameDoe = entryDoe.getKey();
			Integer tfDoe = entryDoe.getValue();
			Integer tfDoa = null;
			for (Map.Entry<String, Integer> entryDoa : projectDoa.entrySet()) {
				String projectNameDoa = entryDoa.getKey();
				if (projectNameDoa.equals(projectNameDoe)) {
					tfDoa = entryDoa.getValue();
					break;
				}
			}
			ProjectDoaDoe projectDoaDoe = new ProjectDoaDoe(projectNameDoe, tfDoa, tfDoe);
			projects.add(projectDoaDoe);
		}
		File file = new File(args[0]+"result_tf.csv");
		FileWriter outputfile;
		try {
			outputfile = new FileWriter(file);
			CSVWriter writer = new CSVWriter(outputfile);
			for (ProjectDoaDoe projectDoaDoe : projects) {
				writer.writeNext(projectDoaDoe.objectString());
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		  
		System.out.println();
	}

	static class ProjectDoaDoe{
		String projectName;
		int doa, doe;
		public ProjectDoaDoe(String projectName, int doa, int doe) {
			super();
			this.projectName = projectName;
			this.doa = doa;
			this.doe = doe;
		}

		public String[] objectString() {
			String[] toString = new String[3];
			toString[0] = projectName;
			toString[1] = String.valueOf(doa);
			toString[2] = String.valueOf(doe);
			return toString;
		}
	}

}
