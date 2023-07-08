package br.com.gitanalyzer.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.Request;
import com.jcabi.http.response.JsonResponse;

import br.com.gitanalyzer.dto.form.CloneRepoForm;
import br.com.gitanalyzer.dto.form.DownloaderForm;
import br.com.gitanalyzer.model.ProjectInfo;
import br.com.gitanalyzer.model.entity.Project;
import br.com.gitanalyzer.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DownloaderService {

	@Value("${configuration.clone.path}")
	private String cloneFolder;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private ProjectService projectService;

	public void download(DownloaderForm form) throws URISyntaxException, InterruptedException {
		try {
			log.info("=========== DOWNLOAD JAVASCRIPT PROJECTS ==================");
			downloader("language:javascript stars:>500", form);
			log.info("=========== DOWNLOAD PYTHON PROJECTS ==================");
			downloader("language:python stars:>500", form);
			log.info("=========== DOWNLOAD JAVA PROJECTS ==================");
			downloader("language:java stars:>500", form);
			log.info("=========== DOWNLOAD TYPESCRIPT PROJECTS ==================");
			downloader("language:typescript stars:>500", form);
			log.info("=========== DOWNLOAD C++ PROJECTS ==================");
			downloader("language:c++ stars:>500", form);
			log.info("=========== DOWNLOADS FINISHED==================");
			projectService.generateCommitFileFolder(form.getPath());
			projectService.setFirstDateFolder(form.getPath());
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
					Project project = new Project(projectInfo.getName(), projectInfo.getFullName(), 
							projectInfo.getLanguage(), form.getPath()+projectInfo.getName()+"/", projectInfo.getDefault_branch(), 
							projectInfo.getStargazers_count());
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
			if(!repoData.isNull("stargazers_count")) {
				p.setStargazers_count(repoData.getInt("stargazers_count"));
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

	public String cloneProject(CloneRepoForm form) throws InvalidRemoteException, TransportException, GitAPIException {
		String projectName = projectService.extractProjectName(form.getUrl());
		projectName = projectName.replace(".git", "");
		File file = new File(cloneFolder+projectName);
		if(form.getBranch() != null && form.getBranch().isEmpty() == false) {
			Git.cloneRepository().setURI(form.getUrl()).setDirectory(file)
			.setBranch(form.getBranch()) 
			.call();
		}else {
			Git.cloneRepository().setURI(form.getUrl()).setDirectory(file)
			.call();
		}
		return cloneFolder+projectName+"/";
	}

}
