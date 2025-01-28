package br.com.knowledgeislands.model.entity;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import br.com.knowledgeislands.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	@Column(unique = true)
	private String email;
	@Column(nullable = false, unique = true)
	private String username;
	@Column(nullable = false)
	private String password;
	@CreationTimestamp
	private LocalDateTime creationDate;
	@Column(length=64)
	private String verificationCode;
	private boolean enabled;
	@ManyToMany(fetch = FetchType.EAGER) 
	@JoinTable( 
			name = "users_roles", 
			joinColumns = @JoinColumn(
					name = "user_id", referencedColumnName = "id"), 
			inverseJoinColumns = @JoinColumn(
					name = "role_id", referencedColumnName = "id"))
	private Set<Role> roles;

	public User(String name, String username, String email, String password, String verificationCode) {
		this.name = name;
		this.username = username;
		this.email = email;
		this.password = password;
		this.verificationCode = verificationCode;
	}
	
	public UserDTO toDTO() {
		return UserDTO.builder()
				.id(id)
				.email(email)
				.password(password)
				.name(name)
				.build();
	}
	
}	
