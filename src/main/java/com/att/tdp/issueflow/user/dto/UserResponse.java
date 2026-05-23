package com.att.tdp.issueflow.user.dto;

import com.att.tdp.issueflow.user.enums.Role;

public record UserResponse(
		Long id,
		String username,
		String email,
		String fullName,
		Role role
) {
}
