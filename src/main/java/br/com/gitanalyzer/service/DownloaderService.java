package br.com.gitanalyzer.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.Request;
import com.jcabi.http.response.JsonResponse;

import br.com.gitanalyzer.main.dto.DownloaderForm;
import br.com.gitanalyzer.model.Project;
import br.com.gitanalyzer.model.ProjectInfo;
import br.com.gitanalyzer.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DownloaderService {

	@Autowired
	private ProjectRepository projectRepository;
	
	public void download(DownloaderForm form) {
		try {
			log.info("=========== DOWNLOAD JAVA PROJECTS ==================");
			downloader("language:java stars:>500", form);
			log.info("=========== DOWNLOAD JAVASCRIPT PROJECTS ==================");
			downloader("language:javascript stars:>500", form);
			log.info("=========== DOWNLOAD C++ PROJECTS ==================");
			downloader("language:c++ stars:>500", form);
			log.info("=========== DOWNLOAD PYTHON PROJECTS ==================");
			downloader("language:python stars:>500", form);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void downloader(String query, DownloaderForm form) throws IOException {
		Github github = new RtGithub(form.getToken());
		List<ProjectInfo> projectsInfo = null;
		projectsInfo = searchRepositories(github, form.getNumRepository(), query);
		for (ProjectInfo projectInfo : projectsInfo) {
			try {
				System.out.println("Cloning " + projectInfo.getFullName());
				boolean flag = cloneIfNotExists(projectInfo, form.getPath());
				if(flag) {
					Project project = new Project(projectInfo.getName(), projectInfo.getLanguage());
					projectRepository.save(project);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean cloneIfNotExists(ProjectInfo projectInfo, String path) throws Exception {
		String cloneUrl = projectInfo.getCloneUrl();
		String branch = projectInfo.getDefault_branch();
		File folder = new File(path+projectInfo.getName()+"/");
		if(folder.exists() == false) {
			Git.cloneRepository()
			.setURI(cloneUrl)
			.setDirectory(new File(path+projectInfo.getName()+"/"))
			.setBranch(branch).call();
			return true;
		}
		return false;
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
