package br.com.gitanalyzer.service;

import org.springframework.stereotype.Service;

import br.com.gitanalyzer.extractors.HistoryCommitsExtractor;
import br.com.gitanalyzer.main.dto.HashNumberYears;

@Service
public class HistoryCommitsService {
	
	public HashNumberYears saveHistory(HashNumberYears form) {
		HistoryCommitsExtractor extractor = new HistoryCommitsExtractor();
		extractor.commitsHashs(form);
		return form;
	}
}
