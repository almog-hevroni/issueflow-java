package com.att.tdp.issueflow.security.auth.dto;

public record LoginResponse(
		String accessToken,
		String tokenType,
		long expiresIn
) {
}
