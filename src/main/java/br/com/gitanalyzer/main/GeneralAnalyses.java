package br.com.gitanalyzer.main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;


public class GeneralAnalyses {

	public static void main(String[] args) {
		List<DoaResultVO> doas = new ArrayList<DoaResultVO>();
		List<DoeResultVO> does = new ArrayList<DoeResultVO>();
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
		for (String[] string : doaFile) {
			String url = string[0];
			String[] urlSplited = url.split("/");
			doas.add(new DoaResultVO(urlSplited[urlSplited.length-1], Integer.parseInt(string[1]), Integer.parseInt(string[2]), Integer.parseInt(string[3])));
		}
		for (String[] string : doeFile) {
			does.add(new DoeResultVO(string[0], Integer.parseInt(string[1]), Integer.parseInt(string[2])));
		}
		List<ProjectDoaDoe> projects = new ArrayList<ProjectDoaDoe>();
		for (DoeResultVO doeResult : does) {
			String projectNameDoe = doeResult.name;
			int tfDoe = doeResult.tf;
			int numAuthorsDoe = doeResult.numAuthors;
			int tfDoa = 0, numDevs = 0, numAuthors = 0;
			for (DoaResultVO doaResult : doas) {
				String projectNameDoa = doaResult.name;
				if (projectNameDoa.equals(projectNameDoe)) {
					tfDoa = doaResult.tf;
					numDevs = doaResult.numDevs;
					numAuthors = doaResult.numAuthors;
					break;
				}
			}
			ProjectDoaDoe projectDoaDoe = new ProjectDoaDoe(projectNameDoe, tfDoa, tfDoe, numDevs, numAuthors, numAuthorsDoe);
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
		int doa, doe, numDevs, numAuthorsDoa, numAuthorsDoe;

		public ProjectDoaDoe(String projectName, int doa, int doe, int numDevs, int numAuthorsDoa,
				int numAuthorsDoe) {
			super();
			this.projectName = projectName;
			this.doa = doa;
			this.doe = doe;
			this.numDevs = numDevs;
			this.numAuthorsDoa = numAuthorsDoa;
			this.numAuthorsDoe = numAuthorsDoe;
		}

		public String[] objectString() {
			String[] toString = new String[6];
			toString[0] = projectName;
			toString[1] = String.valueOf(doa);
			toString[2] = String.valueOf(doe);
			toString[3] = String.valueOf(numDevs);
			toString[4] = String.valueOf(numAuthorsDoa);
			toString[5] = String.valueOf(numAuthorsDoe);
			return toString;
		}
	}

	static class DoaResultVO {
		String name;
		int tf, numDevs, numAuthors;
		public DoaResultVO(String name, int tf, int numDevs, int numAuthors) {
			super();
			this.name = name;
			this.tf = tf;
			this.numDevs = numDevs;
			this.numAuthors = numAuthors;
		}
	}

	static class DoeResultVO {
		String name;
		int tf, numAuthors;
		public DoeResultVO(String name, int tf, int numAuthors) {
			super();
			this.name = name;
			this.tf = tf;
			this.numAuthors = numAuthors;
		}
	}

}
