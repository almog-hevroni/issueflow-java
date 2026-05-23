package com.att.tdp.issueflow.user.dto;

import com.att.tdp.issueflow.user.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
		@NotBlank @Size(max = 100) String username,
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank @Size(max = 255) String fullName,
		@NotNull Role role,
		@Size(min = 8, max = 255, message = "password must be between 8 and 255 characters")
		String password
) {
}
