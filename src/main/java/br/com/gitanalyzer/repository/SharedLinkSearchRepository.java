package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gitanalyzer.model.entity.SharedLinkSearch;

public interface SharedLinkSearchRepository extends JpaRepository<SharedLinkSearch, Long> {

}
