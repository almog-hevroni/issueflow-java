package com.att.tdp.issueflow.security.auth;

import com.att.tdp.issueflow.security.auth.dto.AuthMeResponse;
import com.att.tdp.issueflow.security.auth.dto.LoginResponse;
import com.att.tdp.issueflow.security.jwt.JwtService;
import com.att.tdp.issueflow.security.jwt.JwtTokenDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final TokenRevocationService tokenRevocationService;

	public AuthService(
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			TokenRevocationService tokenRevocationService
	) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.tokenRevocationService = tokenRevocationService;
	}

	public LoginResponse login(String username, String password) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(username, password)
		);
		AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
		String token = jwtService.generateToken(principal.getId(), principal.getUsername(), principal.getUser().getRole());
		return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds());
	}

	@Transactional
	public void logout(String rawToken) {
		JwtTokenDetails tokenDetails = jwtService.extractTokenDetails(rawToken);
		tokenRevocationService.revoke(rawToken, tokenDetails);
		tokenRevocationService.purgeExpiredRevocations();
	}

	public AuthMeResponse currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
		return new AuthMeResponse(
				principal.getId(),
				principal.getUsername(),
				principal.getEmail(),
				principal.getFullName(),
				principal.getUser().getRole()
		);
	}
}
