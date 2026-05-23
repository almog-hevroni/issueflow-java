package com.att.tdp.issueflow.security.auth;

import com.att.tdp.issueflow.security.auth.dto.AuthMeResponse;
import com.att.tdp.issueflow.security.auth.dto.LoginRequest;
import com.att.tdp.issueflow.security.auth.dto.LoginResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	@SecurityRequirements
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request.username(), request.password());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
		String token = extractBearerToken(authorizationHeader);
		authService.logout(token);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/me")
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
