package br.com.gitanalyzer.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitanalyzer.dto.LoginRequestDTO;
import br.com.gitanalyzer.dto.MessageResponseDTO;
import br.com.gitanalyzer.dto.SignupRequestDTO;
import br.com.gitanalyzer.dto.UserInfoResponseDTO;
import br.com.gitanalyzer.model.UserDetailsImpl;
import br.com.gitanalyzer.model.entity.Role;
import br.com.gitanalyzer.model.entity.User;
import br.com.gitanalyzer.model.enums.RoleEnum;
import br.com.gitanalyzer.repository.RoleRepository;
import br.com.gitanalyzer.repository.UserRepository;
import br.com.gitanalyzer.utils.JwtUtils;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class LoginController {

	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtUtils jwtuUtils;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder encoder;
	@Autowired
	private RoleRepository roleRepository;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		ResponseCookie jwtCookie = jwtuUtils.generateJwtCookie(userDetails);
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
				.body(new UserInfoResponseDTO(userDetails.getId(),
						userDetails.getUsername(),
						userDetails.getEmail(),
						roles));
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequestDTO signUpRequest){
		if(userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Username is already taken!"));
		}
		if(userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Email is already taken!"));
		}
		User user = new User(signUpRequest.getUsername(),
				signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName(RoleEnum.ROLE_USER);
			if(userRole == null) {
				throw new RuntimeException("Error: Role is not found.");
			}
			roles.add(userRole);
		}

		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponseDTO("User registered successfully!"));
	}

	@PostMapping("/signout")
	public ResponseEntity<?> logout(){
		ResponseCookie cookie = jwtuUtils.getCleanJwtCookie();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(new MessageResponseDTO("You've been signed out"));
	}

}
