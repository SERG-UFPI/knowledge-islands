package br.com.gitanalyzer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.TruckFactorProcessService;

@RestController
@RequestMapping("/truck-factor-process")
public class TruckFactorProcessController {

	@Autowired
	private TruckFactorProcessService service;

	@GetMapping("/{id}")
	public ResponseEntity<?> getProcessesByUserId(Long id) throws Exception{
		return ResponseEntity.ok(service.getByUserId(id));
	}
}
