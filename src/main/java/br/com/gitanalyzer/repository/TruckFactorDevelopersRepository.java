package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.TruckFactorDevelopers;

@Repository
public interface TruckFactorDevelopersRepository extends JpaRepository<TruckFactorDevelopers, Long> {

	boolean existsByTruckFactorIdAndNameAndEmail(Long id, String name, String email);
}
