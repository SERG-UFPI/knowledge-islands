
package br.com.gitanalyzer.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class KnowledgeIslandsUtils {

	public static final String pathInputMlFile = new String("/home/otavio/analiseR/doutorado/master_data/ml_models/input.csv");
	public static final String pathScriptMlFile = new String("/home/otavio/analiseR/doutorado/master_data/ml_models/predictionScript.R");
	public static final String pathOutputMlFile = new String("/home/otavio/analiseR/doutorado/master_data/ml_models/output.csv");

	public static final String pathCommitFilesLog = new String("/home/otavio/estudo_ihealth/ihealth/commitFilesLog.csv");
	public static final String pathCommitFilesFrequencyLog = new String("/home/otavio/Desktop/ihealth/ihealth/commitFilesFrequencyLog.csv");

	public static List<String> invalidPaths = Arrays.asList(new String[]{"/dev/null"});

	/**
	 * DOE coefficients
	 */
	public static final double interceptDoe = 5.28223;
	public static final double addsCoefDoe = 0.23173;
	public static final double faCoefDoe = 0.36151;
	public static final double numDaysCoefDoe = -0.19421;
	public static final double sizeCoefDoe = -0.28761;

	/**
	 * DOA coefficients
	 */
	public static final double interceptDoa = 3.293;
	public static final double faCoefDoa = 1.098;
	public static final double dlCoefDoa = 0.164;
	public static final double acCoefDoa = -0.321;

	public static final String ADD = new String("ADD");
	public static final String MODIFY = new String("MODIFY");
	public static final String DELETE = new String("DELETE");
	public static final String RENAME = new String ("RENAME");

	public static final String linguistFileName = new String("linguistfiles.log");
	public static final String clocFileName = new String("cloc_info.log");
	public static final String allFilesFileName = new String("filelist.log");
	public static final String commitFileName = new String("commitinfo.log");
	public static final String commitFileFileName = new String("commitfileinfo.log");
	public static final String diffFileName = new String("diff.log");
	public static final String logFile = new String("log.log");

	public static final String truckFactorResultFile = new String("truck_factor.log");
	public static final String truckFactorHistoryFile = new String("truckFactorHistoryFile.log");
	public static final String developersProjectFileName = new String("truck_factor_devs.log");

	public static final double normalizedThresholdMantainerDOA = 0.75;
	public static final double normalizedThresholdMantainerDOE = 0.7;
	public static final double thresholdMantainerDOA = 3.293;

	public static final int quantKnowledgedDevsByFile = 3;

	public static final double thresholdPercentExpert = 0.01;

	public static final int intervalYearsProjectConsideredInactivate = 1;
	public static final int intervalYearsProjectAgeFilter = 2;

	public static final int numMaxThreads = 10;
	public static final int numThreadsToTf = 7;
	public static final int numThreadsToDownload = 5;

	public static final String commandGetDependencyRepo = "curl -s -H \"Authorization: bearer $TOKEN\" -H \"Accept: application/vnd.github.hawkgirl-preview+json\" -X POST -d '{\"query\":\"{ repository(owner:\\\"$OWNER\\\",name:\\\"$PROJECT\\\") { dependencyGraphManifests { edges { node { dependencies { nodes { packageName requirements hasDependencies packageManager repository { name nameWithOwner } } } } } } } }\"}' https://api.github.com/graphql";

	public static final String noreply = ".noreply";
	public static final String chatGptShare = "https://chat.openai.com/share/";

	public static final String openAiJsonStart = "window.__remixContext = ";
	public static final String openAiJsonEnd = "</script><script nonce=";
	public static final String openAiCodeJsonDelimiter = "```";

	public static final String regexOpenAiRegex = "https:\\/\\/chat\\.openai\\.com\\/share\\/[a-zA-Z0-9-]{36}";

	public static final String githubApiBaseUrl = "https://api.github.com";

	public static final String repeatedRepoSuffix = "RepeatedRepo";

	public static final int pageNotFoundCode = 404;
	public static final String dateStringBeginSharedLink = "2023-01-01";

	public static final String gitHubBaseUrl = "https://github.com/";

	public static List<String> projectsToRemoveInFiltering(){
		List<String> notProjectSoftwareNames = new ArrayList<>();
		notProjectSoftwareNames.add("spring-projects/spring-data-examples");
		notProjectSoftwareNames.add("spring-projects/spring-integration-samples");
		notProjectSoftwareNames.add("spring-projects/spring-security-samples");
		notProjectSoftwareNames.add("spring-projects/spring-amqp-samples");
		notProjectSoftwareNames.add("spring-projects/spring-session-data-mongodb-examples");
		notProjectSoftwareNames.add("spring-projects/spring-ws-samples");
		notProjectSoftwareNames.add("spring-projects/spring-hateoas-examples");
		notProjectSoftwareNames.add("spring-projects/spring-data-book");
		notProjectSoftwareNames.add("eugenp/tutorials");
		notProjectSoftwareNames.add("GrowingGit/GitHub-Chinese-Top-Charts");
		notProjectSoftwareNames.add("521xueweihan/HelloGitHub");
		notProjectSoftwareNames.add("microsoft/Web-Dev-For-Beginners");
		notProjectSoftwareNames.add("ryanmcdermott/clean-code-javascript");
		notProjectSoftwareNames.add("yangshun/tech-interview-handbook");
		notProjectSoftwareNames.add("bregman-arie/devops-exercises");
		notProjectSoftwareNames.add("josephmisiti/awesome-machine-learning");
		notProjectSoftwareNames.add("trekhleb/javascript-algorithms");
		notProjectSoftwareNames.add("spring-projects/security-advisories");
		notProjectSoftwareNames.add("spring-projects/spring-aot-smoke-tests");
		return notProjectSoftwareNames;
	}

	public static String getFileSeparator() {
		return java.nio.file.FileSystems.getDefault().getSeparator();
	}

	public static ExecutorService getExecutorServiceForTf() {
		return Executors.newFixedThreadPool(numThreadsToTf);
	}

	public static ExecutorService getExecutorServiceMax() {
		return Executors.newFixedThreadPool(numMaxThreads);
	}

	public static ExecutorService getExecutorServiceDownload() {
		return Executors.newFixedThreadPool(numThreadsToDownload);
	}

	public static String fixFolderPath(String path) {
		if(!path.substring(path.length() -1).equals(getFileSeparator())) {
			path = path+getFileSeparator();
		}
		return path;
	}

	public static List<String> getProgrammingLanguagesAliasGithub(){
		List<String> alias = new ArrayList<>();
		//		alias.add("python");
		//		alias.add("javascript");
		//		alias.add("java");
		//		alias.add("typescript");
		//		alias.add("csharp");
				alias.add("cpp");
		//		alias.add("c");
		//		alias.add("php");
		//		alias.add("ruby");
		//		alias.add("shell");
		//		alias.add("go");
		//		alias.add("nix");
		//		alias.add("rust");
		//		alias.add("scala");
		//		alias.add("kotlin");
		return alias;
	}

	public static void saveToCSV(String filePath, List<String> headers, List<List<String>> data) {
		Path path = Path.of(filePath);
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			writer.write(String.join(",", headers));
			writer.newLine();
			for (List<String> row : data) {
				writer.write(String.join(",", row));
				writer.newLine();
			}
			log.info("CSV file created successfully at: " + filePath);
		} catch (IOException e) {
			log.error("Error writing CSV file: " + e.getMessage());
		}
	}

	public static List<String> problematicGenAiProject(){
		return Arrays.asList("luhao661/CPP_CODE");
	}

	public static String encodeNonAsciiOnly(String text) {
		StringBuilder encoded = new StringBuilder();
		for (char c : text.toCharArray()) {
			if (c > 127) { // Non-ASCII characters, likely Chinese
				for (byte b : String.valueOf(c).getBytes(StandardCharsets.UTF_8)) {
					encoded.append(String.format("\\%03o", b & 0xFF)); // Encode each byte in octal
				}
			} else {
				encoded.append(c); // Append ASCII characters as-is
			}
		}
		return encoded.toString();
	}

	public static boolean containsNonAscii(String filePath) {
		for (char c : filePath.toCharArray()) {
			if (c > 127) { // Non-ASCII character detected
				return true;
			}
		}
		return false;
	}

	public static String removeEnclosingQuotes(String text) {
		// Check if the string starts and ends with double quotes
		if (text.startsWith("\"") && text.endsWith("\"")) {
			// Remove the enclosing double quotes
			return text.substring(1, text.length() - 1);
		}
		return text; // Return as-is if not enclosed by double quotes
	}

}
