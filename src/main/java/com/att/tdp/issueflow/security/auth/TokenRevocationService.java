package com.att.tdp.issueflow.security.auth;

import com.att.tdp.issueflow.security.auth.entity.RevokedToken;
import com.att.tdp.issueflow.security.auth.repository.RevokedTokenRepository;
import com.att.tdp.issueflow.security.jwt.JwtTokenDetails;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenRevocationService {

	private final RevokedTokenRepository revokedTokenRepository;

	public TokenRevocationService(RevokedTokenRepository revokedTokenRepository) {
		this.revokedTokenRepository = revokedTokenRepository;
	}

	public boolean isRevoked(String jti) {
		return revokedTokenRepository.findByJti(jti).isPresent();
	}

	@Transactional
	public void revoke(String token, JwtTokenDetails tokenDetails) {
		if (isRevoked(tokenDetails.jti())) {
			return;
		}

		RevokedToken revokedToken = new RevokedToken();
		revokedToken.setJti(tokenDetails.jti());
		revokedToken.setTokenHash(hashToken(token));
		revokedToken.setExpiresAt(tokenDetails.expiresAt());
		revokedToken.setRevokedAt(Instant.now());
		revokedTokenRepository.save(revokedToken);
	}

	@Transactional
	public void purgeExpiredRevocations() {
		revokedTokenRepository.deleteAllByExpiresAtBefore(Instant.now());
	}

	private String hashToken(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashBytes);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 algorithm is not available", exception);
		}
	}
}
