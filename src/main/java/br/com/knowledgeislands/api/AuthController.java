package br.com.knowledgeislands.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledgeislands.dto.LoginRequestDTO;
import br.com.knowledgeislands.dto.MessageResponseDTO;
import br.com.knowledgeislands.dto.SignupRequestDTO;
import br.com.knowledgeislands.dto.UserInfoResponseDTO;
import br.com.knowledgeislands.model.UserDetailsImpl;
import br.com.knowledgeislands.service.UserService;
import br.com.knowledgeislands.utils.JwtUtils;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${configuration.allowed.origin}", allowCredentials = "true")
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtUtils jwtuUtils;
	@Autowired
	private UserService userService;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		ResponseCookie jwtCookie = jwtuUtils.generateJwtCookie(userDetails);
		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority()).toList();

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
				.body(new UserInfoResponseDTO(userDetails.getId(),
						userDetails.getUsername(),
						userDetails.getEmail(),
						roles));
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequestDTO signUpRequest) throws Exception{
		userService.registerUser(signUpRequest);
		return ResponseEntity.ok(new MessageResponseDTO("User registered successfully!"));
	}

	@PostMapping("/signout")
	public ResponseEntity<?> logout(){
		ResponseCookie cookie = jwtuUtils.getCleanJwtCookie();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(new MessageResponseDTO("You've been signed out"));
	}

}
