package com.att.tdp.issueflow.security.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "username is required")
		String username,
		@NotBlank(message = "password is required")
		String password
) {
}
