package com.att.tdp.issueflow.security.jwt;

import com.att.tdp.issueflow.user.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private static final String CLAIM_USER_ID = "uid";
	private static final String CLAIM_ROLE = "role";

	private final JwtProperties properties;
	private final SecretKey signingKey;

	public JwtService(JwtProperties properties) {
		this.properties = properties;
		this.signingKey = resolveSigningKey(properties.getSecret());
	}

	public String generateToken(Long userId, String username, Role role) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(properties.getExpirationSeconds());
		return Jwts.builder()
				.id(UUID.randomUUID().toString())
				.subject(username)
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiresAt))
				.claims(Map.of(
						CLAIM_USER_ID, userId,
						CLAIM_ROLE, role.name()
				))
				.signWith(signingKey)
				.compact();
	}

	public boolean isTokenValid(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException exception) {
			return false;
		}
	}

	public JwtPrincipal extractPrincipal(String token) {
		Claims claims = parseClaims(token);
		Long userId = claims.get(CLAIM_USER_ID, Long.class);
		String username = claims.getSubject();
		Role role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));
		return new JwtPrincipal(userId, username, role);
	}

	public JwtTokenDetails extractTokenDetails(String token) {
		Claims claims = parseClaims(token);
		return new JwtTokenDetails(claims.getId(), claims.getExpiration().toInstant());
	}

	public long getExpirationSeconds() {
		return properties.getExpirationSeconds();
	}

	private Claims parseClaims(String token) {
		Jws<Claims> parsed = Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token);
		return parsed.getPayload();
	}

	private SecretKey resolveSigningKey(String rawSecret) {
		try {
			byte[] decoded = Decoders.BASE64.decode(rawSecret);
			return Keys.hmacShaKeyFor(decoded);
		} catch (RuntimeException exception) {
			return Keys.hmacShaKeyFor(rawSecret.getBytes(StandardCharsets.UTF_8));
		}
	}
}
