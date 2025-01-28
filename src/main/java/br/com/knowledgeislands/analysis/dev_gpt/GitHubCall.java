package br.com.knowledgeislands.analysis.dev_gpt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GitHubCall {

	public static int numberSearchCall = 0;
	public static int numberGeneralCall = 0;

	public static String searchCall(String[] command) throws InterruptedException, IOException {
		numberSearchCall++;
		if(numberSearchCall <= 9) {
			return callCommand(command);
		}else {
			Thread.sleep(61*1000);
			numberSearchCall = 0;
			return searchCall(command);
		}
	}

	public static String generalCall(String[] command) throws InterruptedException, IOException {
		numberGeneralCall++;
		if(true) {
			return callCommand(command);
		}else {
			Thread.sleep(61*1000);
			numberGeneralCall = 0;
			return searchCall(command);
		}
	}

	private static String callCommand(String[] command) throws IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Process process = processBuilder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		StringBuilder content = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			content.append(line);
		}
		process.waitFor();
		reader.close();
		return content.toString();
	}
	
	
//	gh api graphql -f query='query {
//			  repository(owner: "OtavioCury", name: "knowledge-islands") {
//			    object(expression: "main") {
//			      ... on Commit {
//			        blame(path: "src/main/java/br/com/gitanalyzer/utils/JwtUtils.java") {
//			          ranges {
//			            commit {
//			              author {
//			                name
//			              }
//			            }
//			            startingLine
//			            endingLine
//			            age
//			          }
//			        }
//			      }
//			    }
//			  }
//			}'
}
