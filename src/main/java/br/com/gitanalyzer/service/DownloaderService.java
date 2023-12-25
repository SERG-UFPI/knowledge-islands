package br.com.gitanalyzer.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import br.com.gitanalyzer.dto.form.DownloaderPerLanguageForm;
import br.com.gitanalyzer.dto.form.DownloaderPerOrgForm;
import br.com.gitanalyzer.enums.LanguageEnum;
import br.com.gitanalyzer.model.ProjectInfo;
import br.com.gitanalyzer.model.entity.Project;
import br.com.gitanalyzer.repository.ProjectRepository;
import br.com.gitanalyzer.utils.AsyncUtils;
import br.com.gitanalyzer.utils.Constants;

@Service
public class DownloaderService {

	@Value("${configuration.clone.path}")
	private String cloneFolder;
	@Value("${configuration.github.token}")
	private String token;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private ProjectService projectService;

	public void downloadPerLanguage(DownloaderPerLanguageForm form) throws URISyntaxException, InterruptedException {
		try {
			if(form.getLanguage().equals(LanguageEnum.ALL)) {
				ExecutorService executorService = Executors.newFixedThreadPool(5);
				List<CompletableFuture<Void>> futures = new ArrayList<>();
				for (LanguageEnum language : LanguageEnum.values()) {
					System.out.println("=========== Download "+language.getName()+" project ==================");
					CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
						try {
							downloaderPerLanguage("language:"+language.getName()+" stars:>500", form);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}, executorService);
					futures.add(future);
				}
			}else {
				System.out.println("=========== Download "+form.getLanguage().getName()+" project ==================");
				downloaderPerLanguage("language:"+form.getLanguage().getName()+" stars:>500", form);
			}
			projectService.generateCommitFileFolder(form.getPath());
			projectService.setProjectDatesFolder(form.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void downloadPerOrg(DownloaderPerOrgForm form) throws URISyntaxException, InterruptedException {
		try {
			System.out.println("=========== download from "+form.getOrg()+" org ==================");
			downloaderPerOrg("org:"+form.getOrg(), form);
			projectService.generateCommitFileFolder(form.getPath());
			projectService.setProjectDatesFolder(form.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void downloaderPerOrg(String query, DownloaderPerOrgForm form) throws IOException {
		Github github = new RtGithub(token);
		List<ProjectInfo> projectsInfo = null;
		projectsInfo = searchRepositoriesPerOrganization(github, query);
		cloneAndSaveRepos(projectsInfo, form.getPath());
	}

	public void downloaderPerLanguage(String query, DownloaderPerLanguageForm form) throws IOException {
		Github github = new RtGithub(token);
		List<ProjectInfo> projectsInfo = searchRepositoriesPerLanguage(github, form.getNumRepository(), query);
		cloneAndSaveRepos(projectsInfo, form.getPath());
	}

	private void cloneAndSaveRepos(List<ProjectInfo> projectsInfo, String path) {
		for (ProjectInfo projectInfo : projectsInfo) {
			try {
				System.out.println("Cloning " + projectInfo.getFullName());
				boolean flag = cloneIfNotExists(projectInfo, path);
				if(flag) {
					String projectPath = path+projectInfo.getName()+"/";
					Project project = new Project(projectInfo.getName(), projectInfo.getFullName(), 
							projectInfo.getLanguage(), projectPath, projectInfo.getDefault_branch(), 
							projectInfo.getStargazers_count(), projectService.getCurrentRevisionHash(projectPath));
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

	public List<ProjectInfo> searchRepositoriesPerLanguage(Github github, int numRepository, String query) throws IOException {
		Request request = github.entry()
				.uri().path("/search/repositories")
				.queryParam("q", query )
				.queryParam("sort", "stars")
				.queryParam("order", "desc")
				.queryParam("per_page", String.valueOf(numRepository))
				.back()
				.method(Request.GET);
		return getProjectsInfo(request, query);
	}

	public List<ProjectInfo> searchRepositoriesPerOrganization(Github github, String query) throws IOException {
		Request request = github.entry()
				.uri().path("/search/repositories")
				.queryParam("q", query )
				.queryParam("sort", "stars")
				.queryParam("order", "desc")
				.queryParam("per_page", 100)
				.back()
				.method(Request.GET);
		return getProjectsInfo(request, query);
	}

	private List<ProjectInfo> getProjectsInfo(Request request, String query) throws IOException{
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
