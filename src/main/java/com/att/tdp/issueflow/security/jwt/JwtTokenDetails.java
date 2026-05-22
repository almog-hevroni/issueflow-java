package com.att.tdp.issueflow.security.jwt;

import java.time.Instant;

public record JwtTokenDetails(
		String jti,
		Instant expiresAt
) {
}
