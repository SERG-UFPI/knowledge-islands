package br.com.gitanalyzer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gitanalyzer.dto.GitRepositoryVersionProcessDTO;
import br.com.gitanalyzer.dto.form.CloneRepoForm;
import br.com.gitanalyzer.model.entity.GitRepositoryVersionProcess;
import br.com.gitanalyzer.model.entity.User;
import br.com.gitanalyzer.model.enums.GitRepositoryVersionProcessStageEnum;
import br.com.gitanalyzer.repository.GitRepositoryVersionProcessRepository;
import br.com.gitanalyzer.repository.UserRepository;

@Service
public class GitRepositoryVersionProcessService {
	@Autowired
	private GitRepositoryVersionProcessRepository repository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TruckFactorService truckFactorService;

	public List<GitRepositoryVersionProcessDTO> getByUserId(Long id) throws Exception{
		if(userRepository.existsById(id) == false) {
			throw new Exception("Usuário com id {"+id+"} não encontrado");
		}else {
			List<GitRepositoryVersionProcess> processes = repository.findByUserId(id); 
			return processes.stream().map(p -> p.toDTO()).toList();
		}
	}

	public GitRepositoryVersionProcess getProcessesById(Long id) {
		return repository.findById(id).get();
	}

	@Transactional
	public GitRepositoryVersionProcess cloneAndSaveGitRepositoryTruckFactor(CloneRepoForm form) throws Exception{
		User user = userRepository.findById(form.getIdUser()).orElse(null);
		if(user == null) {
			throw new Exception("User not found");
		}
		GitRepositoryVersionProcess process = new GitRepositoryVersionProcess(GitRepositoryVersionProcessStageEnum.INITIALIZED, user, form.getCloneUrl());
		repository.save(process);
		truckFactorService.continueProcesses(process, form);
		return process;
	}

	public void removeAll() {
		repository.deleteAll();
	}

}
