package com.att.tdp.issueflow.security.auth;

import com.att.tdp.issueflow.audit.repository.AuditLogRepository;
import com.att.tdp.issueflow.user.entity.User;
import com.att.tdp.issueflow.user.enums.Role;
import com.att.tdp.issueflow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuditLogRepository auditLogRepository;

	@BeforeEach
	void setup() {
		auditLogRepository.deleteAll();
		userRepository.findByUsername("dev-user").ifPresent(existing -> userRepository.deleteById(existing.getId()));
		User user = new User();
		user.setUsername("dev-user");
		user.setEmail("dev-user@example.com");
		user.setFullName("Developer User");
		user.setRole(Role.DEVELOPER);
		user.setPasswordHash(passwordEncoder.encode("Password123!"));
		userRepository.saveAndFlush(user);
	}

	@Test
	void loginShouldReturnJwtPayload() throws Exception {
		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "dev-user",
								  "password": "Password123!"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isString())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(3600));
	}

	@Test
	void loginWithInvalidCredentialsShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "dev-user",
								  "password": "wrong-password"
								}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpointShouldRequireAuthentication() throws Exception {
		mockMvc.perform(get("/auth/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void meEndpointShouldReturnCurrentUserWhenTokenIsValid() throws Exception {
		String accessToken = obtainAccessToken("dev-user", "Password123!");

		mockMvc.perform(get("/auth/me")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("dev-user"))
				.andExpect(jsonPath("$.email").value("dev-user@example.com"))
				.andExpect(jsonPath("$.fullName").value("Developer User"))
				.andExpect(jsonPath("$.role").value("DEVELOPER"));
	}

	@Test
	void logoutShouldRevokeToken() throws Exception {
		String accessToken = obtainAccessToken("dev-user", "Password123!");

		mockMvc.perform(post("/auth/logout")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk());

		mockMvc.perform(get("/auth/me")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isUnauthorized());
	}

	private String obtainAccessToken(String username, String password) throws Exception {
		String response = mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "%s",
								  "password": "%s"
								}
								""".formatted(username, password)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		int tokenFieldStart = response.indexOf("\"accessToken\":\"") + "\"accessToken\":\"".length();
		int tokenFieldEnd = response.indexOf("\",\"tokenType\"");
		return response.substring(tokenFieldStart, tokenFieldEnd);
	}
}
