package com.att.tdp.issueflow.security.auth.dto;

import com.att.tdp.issueflow.user.enums.Role;

public record AuthMeResponse(
		Long id,
		String username,
		String email,
		String fullName,
		Role role
) {
}
