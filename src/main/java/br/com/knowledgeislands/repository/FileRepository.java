package br.com.knowledgeislands.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.knowledgeislands.model.entity.File;

@Repository
public interface FileRepository extends JpaRepository<File, Long>{
	@Query("SELECT DISTINCT f.language from File f where f.language IS NOT NULL")
	List<String> findDistinctLanguages();
	File findByUrl(String url);
}
