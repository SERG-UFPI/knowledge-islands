package br.com.knowledgeislands.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledgeislands.model.entity.ErrorLog;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

}
