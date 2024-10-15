package br.com.gitanalyzer.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gitanalyzer.model.entity.GitRepositoryDependency;
import br.com.gitanalyzer.model.entity.GitRepositoryVersion;
import br.com.gitanalyzer.repository.GitRepositoryVersionRepository;
import br.com.gitanalyzer.utils.GitRepositoryUtils;

@Service
public class GitRepositoryDependencyService {

	@Value("${configuration.github.token}")
	private String token;

	@Autowired
	private GitRepositoryVersionRepository projectVersionRepository;

	public void getProjectVersionAndSetDependency(Long id) {
		GitRepositoryVersion version = projectVersionRepository.findById(id).get();
		getDependenciesProjectVersion(version.getGitRepository().getFullName());
	}

	public List<GitRepositoryDependency> getDependenciesProjectVersion(String projectFullName) {
		List<GitRepositoryDependency> dependencies = new ArrayList<>();
		try {
			String owner = GitRepositoryUtils.getOwnerNameProject(projectFullName);
			String name = GitRepositoryUtils.getNameProject(projectFullName);
			String command = br.com.gitanalyzer.utils.KnowledgeIslandsUtils.commandGetDependencyRepo.replace("$TOKEN", token).replace("$OWNER", owner).replace("$PROJECT", name);
			ProcessBuilder pb = new ProcessBuilder(new String[] {"bash", "-l", "-c", command});
			pb.redirectErrorStream(true);
			Process process = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String data = reader.readLine();
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(data);
			JsonNode edges = jsonNode.get("data").get("repository").get("dependencyGraphManifests").get("edges");
			for(JsonNode edge: edges) {
				JsonNode nodes = edge.get("node").get("dependencies");
				for (JsonNode node : nodes) {
					for (JsonNode packageInfo : node) {
						String packageManager = packageInfo.get("packageManager").asText();
						JsonNode repository = packageInfo.get("repository");
						if(!repository.isNull() && packageManager != null) {
							String nameRepository = repository.get("name").asText();
							String fullNameRepository = repository.get("nameWithOwner").asText();
							boolean contains = false;
							for (GitRepositoryDependency projectDependency : dependencies) {
								if(projectDependency.getRepositoryFullName().equals(fullNameRepository)) {
									contains = true;
									break;
								}
							}
							if(contains == false && !fullNameRepository.equals(projectFullName)) {
								GitRepositoryDependency dependency = new GitRepositoryDependency(nameRepository, packageManager, fullNameRepository);
								dependencies.add(dependency);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dependencies;
	}

}