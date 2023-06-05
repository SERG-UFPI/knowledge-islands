package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gitanalyzer.model.entity.TruckFactorProcess;

public interface TruckFactorProcessRepository extends JpaRepository<TruckFactorProcess, Long>{
	
	List<TruckFactorProcess> findByUserId(Long id);
	
}
