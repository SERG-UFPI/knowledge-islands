package br.com.gitanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gitanalyzer.model.entity.ErrorLog;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

}
