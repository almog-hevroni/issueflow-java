package com.att.tdp.issueflow.security.jwt;

import com.att.tdp.issueflow.user.enums.Role;

public record JwtPrincipal(
		Long userId,
		String username,
		Role role
) {
}
