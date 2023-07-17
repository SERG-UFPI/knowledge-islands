package br.com.gitanalyzer.service;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.gitanalyzer.dto.SignupRequestDTO;
import br.com.gitanalyzer.model.entity.Role;
import br.com.gitanalyzer.model.entity.User;
import br.com.gitanalyzer.model.enums.RoleEnum;
import br.com.gitanalyzer.repository.RoleRepository;
import br.com.gitanalyzer.repository.UserRepository;
import net.bytebuddy.utility.RandomString;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder encoder;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private JavaMailSender mailSender;
	@Value("${spring.mail.username}")
	private String emailSender;
	@Value("${configuration.app.front-url}")
	private String frontUrl;

	public void registerUser(SignupRequestDTO signUpRequest) throws Exception {
		if(userRepository.existsByUsername(signUpRequest.getUsername())) {
			throw new Exception("Error: Username is already taken!");
		}
		if(userRepository.existsByEmail(signUpRequest.getEmail())) {
			throw new Exception("Error: Email is already taken!");
		}
		User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
				signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()), RandomString.make(64));
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
		sendVerificationEmail(user, frontUrl);
	}

	public void sendVerificationEmail(User user, String frontUrl) throws UnsupportedEncodingException, MessagingException {
		String toAddress = user.getEmail();
		String fromAddress = emailSender;
		String subject = "Please verify your registration";

		String content = "Dear [[name]], <br>"
				+ "Please click the link below to verify your registration:<br>"
				+ "<h3><a href=\"[[url]]\" target=\"_self\"> VERIFY</a></h3>"
				+ "Thank you, <br>"
				+ "Knowledge Islands";
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, "Knowledge Islands");
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", user.getName());
		String verifyUrl = frontUrl+"/verify?code="+user.getVerificationCode();
		content = content.replace("[[url]]", verifyUrl);
		helper.setText(content, true);
		mailSender.send(message);
	}

	public boolean verify(String verificationCode) {
		User user = userRepository.findByVerificationCode(verificationCode);
		if(user == null || user.isEnabled()) {
			return false;
		}else {
			user.setVerificationCode(null);
			user.setEnabled(true);
			userRepository.save(user);
			return true;
		}
	}

}
