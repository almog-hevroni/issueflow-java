package com.att.tdp.issueflow.security.jwt;

import com.att.tdp.issueflow.user.enums.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

	@Test
	void generatedTokenShouldBeValidAndContainClaims() {
		JwtProperties properties = new JwtProperties();
		properties.setSecret("test-secret-key-test-secret-key-32chars");
		properties.setExpirationSeconds(3600);

		JwtService jwtService = new JwtService(properties);
		String token = jwtService.generateToken(42L, "dev-user", Role.DEVELOPER);

		assertTrue(jwtService.isTokenValid(token));

		JwtPrincipal principal = jwtService.extractPrincipal(token);
		assertEquals(42L, principal.userId());
		assertEquals("dev-user", principal.username());
		assertEquals(Role.DEVELOPER, principal.role());

		JwtTokenDetails tokenDetails = jwtService.extractTokenDetails(token);
		assertNotNull(tokenDetails.jti());
		assertTrue(tokenDetails.expiresAt().isAfter(java.time.Instant.now()));
	}

	@Test
	void malformedTokenShouldBeRejected() {
		JwtProperties properties = new JwtProperties();
		properties.setSecret("test-secret-key-test-secret-key-32chars");
		properties.setExpirationSeconds(3600);

		JwtService jwtService = new JwtService(properties);
		assertFalse(jwtService.isTokenValid("not-a-jwt-token"));
	}
}
