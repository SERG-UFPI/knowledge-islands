package br.com.gitanalyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gitanalyzer.model.entity.SharedLink;

@Repository
public interface SharedLinkRepository extends JpaRepository<SharedLink, Long> {

	List<SharedLink> findByRepositoryNotNull();
	List<SharedLink> findByRepositoryId(Long id);

}
