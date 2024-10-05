package br.com.gitanalyzer.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.Request;
import com.jcabi.http.response.JsonResponse;

import br.com.gitanalyzer.dto.form.CloneRepoForm;
import br.com.gitanalyzer.dto.form.DownloaderPerLanguageForm;
import br.com.gitanalyzer.dto.form.DownloaderPerOrgForm;
import br.com.gitanalyzer.model.entity.GitRepository;
import br.com.gitanalyzer.model.entity.ProjectGitHub;
import br.com.gitanalyzer.model.entity.User;
import br.com.gitanalyzer.model.enums.LanguageEnum;
import br.com.gitanalyzer.repository.GitRepositoryRepository;
import br.com.gitanalyzer.repository.UserRepository;
import br.com.gitanalyzer.utils.AsyncUtils;
import br.com.gitanalyzer.utils.Constants;
import br.com.gitanalyzer.utils.SystemUtil;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class DownloaderService {

	@Value("${configuration.permanent-clone.path}")
	private String cloneFolder;
	@Value("${configuration.github.token}")
	private String token;
	@Autowired
	private GitRepositoryRepository gitRepositoryRepository;
	@Autowired
	private GitRepositoryService projectService;
	@Autowired
	private UserRepository userRepository;

	public void downloadPerLanguage(DownloaderPerLanguageForm form) throws URISyntaxException, InterruptedException {
		form.setPath(SystemUtil.fixFolderPath(form.getPath()));
		try {
			if(form.getLanguage().equals(LanguageEnum.ALL)) {
				ExecutorService executorService = AsyncUtils.getExecutorServiceMax();
				List<CompletableFuture<Void>> futures = new ArrayList<>();
				for (LanguageEnum language : LanguageEnum.values()) {
					if(language.equals(LanguageEnum.ALL) == false) {
						CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
							try {
								downloaderPerLanguage("language:"+language.getName()+" stars:>500", form);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}, executorService);
						futures.add(future);
					}
				}
				CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
				executorService.shutdown();
				log.info("=========== End of project cloning ==================");
			}else {
				log.info("=========== Download "+form.getLanguage().getName()+" project ==================");
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
			log.info("=========== download from "+form.getOrg()+" org ==================");
			downloaderPerOrg("org:"+form.getOrg(), form);
			projectService.generateCommitFileFolder(form.getPath());
			projectService.setProjectDatesFolder(form.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void downloaderPerOrg(String query, DownloaderPerOrgForm form) throws IOException {
		Github github = new RtGithub(token);
		List<ProjectGitHub> projectsInfo = null;
		projectsInfo = searchRepositoriesPerOrganization(github, query);
		cloneAndSaveReposOrg(projectsInfo, form.getPath());
	}

	public void downloaderPerLanguage(String query, DownloaderPerLanguageForm form) throws IOException {
		Github github = new RtGithub(token);
		List<ProjectGitHub> projectsInfo = searchRepositoriesPerLanguage(github, form.getNumRepository(), query);
		cloneAndSaveRepos(projectsInfo, form.getPath());
	}

	private void cloneAndSaveReposOrg(List<ProjectGitHub> projectsInfo, String path) {
		ExecutorService executorService = AsyncUtils.getExecutorServiceMax();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (ProjectGitHub projectInfo : projectsInfo) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
				try {
					log.info("Cloning " + projectInfo.getFullName());
					boolean flag = cloneIfNotExists(projectInfo, path);
					if(flag) {
						String projectPath = path+projectInfo.getName()+"/";
						GitRepository project = new GitRepository(projectInfo.getName(), projectInfo.getFullName(), 
								projectInfo.getLanguage(), projectPath, projectInfo.getDefault_branch(), 
								projectInfo.getStargazers_count(), projectService.getCurrentRevisionHash(projectPath));
						gitRepositoryRepository.save(project);
					}
				} catch (Exception e) {
					log.error("Failure on clone "+projectInfo.getFullName());
				}
			}, executorService);
			futures.add(future);
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		executorService.shutdown();
	}

	private void cloneAndSaveRepos(List<ProjectGitHub> projectsInfo, String path) {
		for (ProjectGitHub projectInfo : projectsInfo) {
			try {
				log.info("Cloning " + projectInfo.getFullName());
				boolean flag = cloneIfNotExists(projectInfo, path);
				if(flag) {
					String projectPath = path+projectInfo.getName()+"/";
					GitRepository project = new GitRepository(projectInfo.getName(), projectInfo.getFullName(), 
							projectInfo.getLanguage(), projectPath, projectInfo.getDefault_branch(), 
							projectInfo.getStargazers_count(), projectService.getCurrentRevisionHash(projectPath));
					gitRepositoryRepository.save(project);
				}
			} catch (Exception e) {
				log.error("Failure on clone "+projectInfo.getFullName());
			}
		}
	}

	public boolean cloneIfNotExists(ProjectGitHub projectInfo, String path) throws Exception {
		String cloneUrl = projectInfo.getCloneUrl();
		String branch = projectInfo.getDefault_branch();
		File folder = new File(path+projectInfo.getName()+"/");
		if(!folder.exists()) {
			Git.cloneRepository()
			.setURI(cloneUrl)
			.setDirectory(new File(path+projectInfo.getName()+"/"))
			.setBranch(branch).call();
			return true;
		}
		return false;
	}

	public List<ProjectGitHub> searchRepositoriesPerLanguage(Github github, int numRepository, String query) throws IOException {
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

	public List<ProjectGitHub> searchRepositoriesPerOrganization(Github github, String query) throws IOException {
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

	private List<ProjectGitHub> getProjectsInfo(Request request, String query) throws IOException{
		List<ProjectGitHub> projectsInfo = new ArrayList<ProjectGitHub>();
		projectsInfo.addAll(findRepos(request, query));
		return projectsInfo;
	}

	public List<ProjectGitHub> findRepos(Request request, String query) throws IOException {
		JsonArray items = request.fetch().as(JsonResponse.class).json().readObject().getJsonArray("items");

		List<ProjectGitHub> projects = new ArrayList<ProjectGitHub>();
		for (JsonValue item : items) {
			JsonObject repoData = (JsonObject) item;
			ProjectGitHub p = new ProjectGitHub();
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

	@Transactional
	public String cloneRepositoriesWithSharedLinks() throws URISyntaxException, IOException, InterruptedException {
		List<GitRepository> repositories = gitRepositoryRepository.findAllWithSharedLinkConversationNotNullAndCloneUrlNotNullAndCurrentFolderPathIsNull();
		ExecutorService executorService = AsyncUtils.getExecutorServiceMax();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (GitRepository repository : repositories) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
				try {
					repository.setCurrentFolderPath(cloneProject(CloneRepoForm.builder()
							.cloneUrl(repository.getCloneUrl()).branch(repository.getDefaultBranch()).build()));
				}catch (Exception e) {
					e.printStackTrace();
				}
			}, executorService);
			futures.add(future);
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		executorService.shutdown();
		for (GitRepository gitRepository : repositories) {
			if(gitRepository.getCurrentFolderPath() != null) {
				String currentFolderPath = gitRepository.getCurrentFolderPath().substring(0, gitRepository.getCurrentFolderPath().length() - 1);
				if(currentFolderPath.endsWith(Constants.repeatedRepoSuffix)) {
					gitRepository.setName(gitRepository.getName()+Constants.repeatedRepoSuffix);
				}
			}
		}
		gitRepositoryRepository.saveAll(repositories);
		return "Downloads finished in "+cloneFolder;
	}

	public String cloneProject(CloneRepoForm form) {
		String cloneFolderModified = cloneFolder;
		if(form.getIdUser() != null) {
			Optional<User> op = userRepository.findById(form.getIdUser());
			if(op.isPresent()) {
				cloneFolderModified = cloneFolderModified+op.get().getUsername().toLowerCase()+"/";
			}
		}
		String projectName = projectService.extractProjectName(form.getCloneUrl());
		projectName = projectName.replace(".git", "");
		Git git = cloneProjectFromFile(projectName, 0, form, cloneFolderModified);
		String path = git.getRepository().getDirectory().getAbsolutePath().replace(".git", "");
		git.close();
		return path;
	}

	private Git cloneProjectFromFile(String projectName, int repeatedNumber, CloneRepoForm form, String cloneFolder) {
		File file = null;
		if(repeatedNumber != 0) {
			file = new File(cloneFolder+projectName+"RepeatedRepo"+repeatedNumber);
		}else {
			file = new File(cloneFolder+projectName);
		}
		Git git = null;
		try {
			if(form.getBranch() != null && form.getBranch().isEmpty() == false) {
				git = Git.cloneRepository().setURI(form.getCloneUrl()).setDirectory(file)
						.setBranch(form.getBranch()) 
						.call();
			}else {
				git = Git.cloneRepository().setURI(form.getCloneUrl()).setDirectory(file)
						.call();
			}
		}catch(JGitInternalException e) {
			log.error(form.getCloneUrl());
			e.printStackTrace();
			repeatedNumber = repeatedNumber+1;
			git = cloneProjectFromFile(projectName, repeatedNumber, form, cloneFolder);
		}catch(GitAPIException e) {
			log.error(form.getCloneUrl());
			e.printStackTrace();
		}
		return git;
	}

}
