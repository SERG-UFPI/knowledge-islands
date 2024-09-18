package br.com.gitanalyzer.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.service.UserService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class UserController {

	@Autowired
	private UserService service;

	@GetMapping("/verify")
	public ResponseEntity<?> verify(@RequestParam String code){
		return ResponseEntity.ok(service.verify(code));
	}
	
}
