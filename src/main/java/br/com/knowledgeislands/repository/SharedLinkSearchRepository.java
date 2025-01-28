package br.com.knowledgeislands.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledgeislands.model.entity.SharedLinkSearch;

public interface SharedLinkSearchRepository extends JpaRepository<SharedLinkSearch, Long> {

}
