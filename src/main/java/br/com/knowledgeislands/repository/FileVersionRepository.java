package br.com.knowledgeislands.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.FileVersion;

@Repository
public interface FileVersionRepository extends JpaRepository<FileVersion, Long>{

}
