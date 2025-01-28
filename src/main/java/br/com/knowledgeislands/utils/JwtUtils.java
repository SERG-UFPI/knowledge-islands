package br.com.knowledgeislands.utils;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import br.com.knowledgeislands.model.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class JwtUtils {

	@Value("${configuration.app.jwtSecret}")
	private String jwtSecret;
	@Value("${configuration.app.jwtExpirationMs}")
	private String jwtExpirationMs;
	@Value("${configuration.app.jwtCookieName}")
	private String jwtCookie;

	public String getJwtFromCookies(HttpServletRequest request) {
		Cookie cookie = WebUtils.getCookie(request, jwtCookie);
		if(cookie != null) {
			return cookie.getValue();
		}else {
			return null;
		}
	}

	public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
		String jwt = generateTokenFromUsername(userPrincipal.getUsername());
		return ResponseCookie.from(jwtCookie, jwt)
				.path("/api").maxAge(24 * 60 * 60).httpOnly(true).build();
	}

	public ResponseCookie getCleanJwtCookie() {
		ResponseCookie cookie = ResponseCookie.from(jwtCookie, null).path("/api").build();
		return cookie;
	}

	public String getUserNameFromJwtToken(String token) {
		return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
	}

	public boolean validateJwtToken(String authToken) {
		try {
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
			return true;
		} catch (SignatureException e) {
			log.error("Invalid JWT signature: {}");
			e.printStackTrace();
		} catch (MalformedJwtException e) {
			log.error("Invalid JWT token: {}");
			e.printStackTrace();
		} catch (ExpiredJwtException e) {
			log.error("JWT token is expired: {}");
			e.printStackTrace();
		} catch (UnsupportedJwtException e) {
			log.error("JWT token is unsupported: {}");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			log.error("JWT claims string is empty: {}");
			e.printStackTrace();
		}

		return false;
	}

	public String generateTokenFromUsername(String username) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(new Date().getTime()+Long.parseLong(jwtExpirationMs));
		return Jwts.builder()
				.setSubject(username)
				.setIssuedAt(new Date())
				.setExpiration(calendar.getTime())
				.signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
	}

}
