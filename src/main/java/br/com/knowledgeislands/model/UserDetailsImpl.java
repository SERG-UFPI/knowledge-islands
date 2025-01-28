package br.com.knowledgeislands.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.knowledgeislands.model.entity.Role;
import br.com.knowledgeislands.model.entity.User;

public class UserDetailsImpl implements UserDetails{

	private static final long serialVersionUID = 1L;

	private User user;

	public UserDetailsImpl(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<Role> roles = user.getRoles();
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		if(roles != null) {
			roles.forEach(r -> authorities.add(new SimpleGrantedAuthority(r.getName().name())));
		}
		return authorities;
	}

	public Long getId() {
		return user.getId();
	}

	public String getEmail() {
		return user.getEmail();
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}

}
