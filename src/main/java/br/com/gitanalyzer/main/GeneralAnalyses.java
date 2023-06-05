package br.com.gitanalyzer.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;


public class GeneralAnalyses {

	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, ExecutionException {

		File directory = new File("/home/otavio/Desktop/GitAnalyzer/git-analyzer/tmp_projects");
		org.apache.commons.io.FileUtils.deleteDirectory(directory); 
		//		List<DoaResultVO> doas = new ArrayList<DoaResultVO>();
		//		List<DoeResultVO> does = new ArrayList<DoeResultVO>();
		//		List<String[]> doaFile = null;
		//		try (CSVReader reader = new CSVReader(new FileReader(args[0]+"reps_doa.csv"))) {
		//			doaFile = reader.readAll();
		//		} catch (IOException | CsvException e) {
		//			e.printStackTrace();
		//		}
		//		List<String[]> doeFile = null;
		//		try (CSVReader reader = new CSVReader(new FileReader(args[0]+"reps_ml.csv"))) {
		//			doeFile = reader.readAll();
		//		} catch (IOException | CsvException e) {
		//			e.printStackTrace();
		//		}
		//		for (String[] string : doaFile) {
		//			String url = string[0];
		//			String[] urlSplited = url.split("/");
		//			doas.add(new DoaResultVO(urlSplited[urlSplited.length-1], Integer.parseInt(string[1]), Integer.parseInt(string[2]), Integer.parseInt(string[3])));
		//		}
		//		for (String[] string : doeFile) {
		//			does.add(new DoeResultVO(string[0], Integer.parseInt(string[1]), Integer.parseInt(string[2])));
		//		}
		//		List<ProjectDoaDoe> projects = new ArrayList<ProjectDoaDoe>();
		//		for (DoaResultVO doaResult : doas) {
		//			String projectNameDoa = doaResult.name;
		//			int tfDoa = doaResult.tf;
		//			int numDevs = doaResult.numDevs;
		//			int numAuthors = doaResult.numAuthors;
		//			int tfDoe = 0, numAuthorsDoe = 0;
		//			for (DoeResultVO doeResult : does) {
		//				String projectNameDoe = doeResult.name;
		//				if (projectNameDoa.equals(projectNameDoe)) {
		//					tfDoe = doeResult.tf;
		//					numAuthorsDoe = doeResult.numAuthors;
		//					break;
		//				}
		//			}
		//			ProjectDoaDoe projectDoaDoe = new ProjectDoaDoe(projectNameDoa, tfDoa, tfDoe, numDevs, numAuthors, numAuthorsDoe);
		//			projects.add(projectDoaDoe);
		//		}
		//		File file = new File(args[0]+"result_tf_ml.csv");
		//		FileWriter outputfile;
		//		try {
		//			outputfile = new FileWriter(file);
		//			CSVWriter writer = new CSVWriter(outputfile);
		//			for (ProjectDoaDoe projectDoaDoe : projects) {
		//				writer.writeNext(projectDoaDoe.objectString());
		//			}
		//			writer.close();
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
		//
		//		System.out.println();
	}

	public static void printStream (InputStream stream) throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			System.out.println(inputLine);
		in.close();
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
