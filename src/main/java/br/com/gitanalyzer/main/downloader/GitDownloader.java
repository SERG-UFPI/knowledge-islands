package br.com.gitanalyzer.main.downloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.eclipse.jgit.api.Git;

import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.Request;
import com.jcabi.http.response.JsonResponse;

import br.com.gitanalyzer.model.ProjectInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class GitDownloader {

	private String caminho, token;
	private int numRepository;
	
	public GitDownloader(String caminho, String token, int numRepository) {
		super();
		this.caminho = caminho;
		this.token = token;
		this.numRepository = numRepository;
	}
	
	public static void main(String[] args) {
		GitDownloader downloader = new GitDownloader(args[0], args[1], Integer.parseInt(args[2]));
		
		try {
			log.info("=========== DOWNLOAD PROJETOS JAVA ==================");
			downloader.downloader("language:java stars:>500");
			log.info("=========== DOWNLOAD PROJETOS JAVASCRIPT ==================");
			downloader.downloader("language:javascript stars:>500");
			log.info("=========== DOWNLOAD PROJETOS C++ ==================");
			downloader.downloader("language:c++ stars:>500");
			log.info("=========== DOWNLOAD PROJETOS PYTHON ==================");
			downloader.downloader("language:python stars:>500");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void downloader(String query) throws IOException {
		Github github = new RtGithub(token);
		List<ProjectInfo> projectsInfo = null;
		projectsInfo = searchRepositories(github, numRepository, query);
		for (ProjectInfo projectInfo : projectsInfo) {
			try {
				System.out.println("Cloning " + projectInfo.getFullName());
				cloneIfNotExists(projectInfo, caminho);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void cloneIfNotExists(ProjectInfo projectInfo, String caminho) throws Exception {
		String cloneUrl = projectInfo.getCloneUrl();
		String branch = projectInfo.getDefault_branch();
		
		Git.cloneRepository()
				.setURI(cloneUrl)
				.setDirectory(new File(caminho+projectInfo.getName()+"/"))
				.setBranch(branch).call();
	}

	public List<ProjectInfo> searchRepositories(Github github, int numRepository, String query) throws IOException {
		Request request = github.entry()
				.uri().path("/search/repositories")
				.queryParam("q", query )
				.queryParam("sort", "stars")
				.queryParam("order", "desc")
				.queryParam("per_page", String.valueOf(numRepository))
				.back()
				.method(Request.GET);
		List<ProjectInfo> projectsInfo = new ArrayList<ProjectInfo>();
		projectsInfo.addAll(findRepos(request, query));
		return projectsInfo;
	}

	public List<ProjectInfo> findRepos(Request request, String query) throws IOException {
		JsonArray items = request.fetch().as(JsonResponse.class).json().readObject().getJsonArray("items");

		List<ProjectInfo> projects = new ArrayList<ProjectInfo>();
		for (JsonValue item : items) {
			JsonObject repoData = (JsonObject) item;
			ProjectInfo p = new ProjectInfo();
			if (!repoData.isNull("full_name")){
				p.setFullName(repoData.getString("full_name"));
				p.setName(repoData.getString("name"));
			}
			if (!repoData.isNull("default_branch"))
				p.setDefault_branch(repoData.getString("default_branch"));
			if (!repoData.isNull("language"))
				p.setLanguage(repoData.getString("language"));
			p.setCloneUrl(repoData.getString("clone_url"));
			projects.add(p);
		}
		return projects;
	}

}
