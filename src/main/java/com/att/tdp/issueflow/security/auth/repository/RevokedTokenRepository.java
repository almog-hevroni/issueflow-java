package com.att.tdp.issueflow.security.auth.repository;

import com.att.tdp.issueflow.security.auth.entity.RevokedToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

	Optional<RevokedToken> findByJti(String jti);

	void deleteAllByExpiresAtBefore(Instant instant);
}
