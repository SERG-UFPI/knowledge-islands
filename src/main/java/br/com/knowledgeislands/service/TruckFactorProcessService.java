package br.com.knowledgeislands.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.knowledgeislands.dto.TruckFactorProcessDTO;
import br.com.knowledgeislands.model.entity.TruckFactorProcess;
import br.com.knowledgeislands.repository.TruckFactorProcessRepository;
import br.com.knowledgeislands.repository.UserRepository;

@Service
public class TruckFactorProcessService {

	@Autowired
	private TruckFactorProcessRepository repository;
	@Autowired
	private UserRepository userRepository;

//	public List<TruckFactorProcessDTO> getByUserId(Long id) throws Exception{
//		if(userRepository.existsById(id) == false) {
//			throw new Exception("Usuário com id {"+id+"} não encontrado");
//		}else {
//			List<TruckFactorProcess> processes = repository.findByUserId(id); 
//			return processes.stream().map(p -> p.toDTO()).toList();
//		}
//	}
}
