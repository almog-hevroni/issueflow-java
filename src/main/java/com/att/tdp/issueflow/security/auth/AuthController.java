package com.att.tdp.issueflow.security.auth;

import com.att.tdp.issueflow.security.auth.dto.AuthMeResponse;
import com.att.tdp.issueflow.security.auth.dto.LoginRequest;
import com.att.tdp.issueflow.security.auth.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import com.att.tdp.issueflow.common.api.ApiErrorResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "JWT authentication endpoints")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	@SecurityRequirements
	@Operation(summary = "Login and obtain JWT")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Authenticated successfully"),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid request body",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "Invalid username or password",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request.username(), request.password());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	@Operation(summary = "Logout and revoke current token")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Logged out successfully"),
			@ApiResponse(
					responseCode = "400",
					description = "Authorization header is missing or malformed",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
		String token = extractBearerToken(authorizationHeader);
		authService.logout(token);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/me")
	@Operation(summary = "Get current authenticated user profile")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User profile returned"),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<AuthMeResponse> me() {
		AuthMeResponse response = authService.currentUser();
		return ResponseEntity.ok(response);
	}

	private String extractBearerToken(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authorization header must be Bearer token");
		}
		return authorizationHeader.substring(7);
	}
}
