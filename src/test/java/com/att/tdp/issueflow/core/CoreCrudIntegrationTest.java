package com.att.tdp.issueflow.core;

import com.att.tdp.issueflow.attachment.repository.AttachmentRepository;
import com.att.tdp.issueflow.audit.entity.AuditLog;
import com.att.tdp.issueflow.audit.repository.AuditLogRepository;
import com.att.tdp.issueflow.comment.repository.CommentMentionRepository;
import com.att.tdp.issueflow.comment.repository.CommentRepository;
import com.att.tdp.issueflow.project.repository.ProjectRepository;
import com.att.tdp.issueflow.security.auth.repository.RevokedTokenRepository;
import com.att.tdp.issueflow.ticket.dependency.repository.TicketDependencyRepository;
import com.att.tdp.issueflow.ticket.repository.TicketRepository;
import com.att.tdp.issueflow.user.entity.User;
import com.att.tdp.issueflow.user.enums.Role;
import com.att.tdp.issueflow.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CoreCrudIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private CommentMentionRepository commentMentionRepository;

	@Autowired
	private TicketDependencyRepository ticketDependencyRepository;

	@Autowired
	private AttachmentRepository attachmentRepository;

	@Autowired
	private AuditLogRepository auditLogRepository;

	@Autowired
	private RevokedTokenRepository revokedTokenRepository;

	@BeforeEach
	void setup() {
		auditLogRepository.deleteAll();
		commentMentionRepository.deleteAll();
		commentRepository.deleteAll();
		ticketDependencyRepository.deleteAll();
		attachmentRepository.deleteAll();
		ticketRepository.deleteAll();
		projectRepository.deleteAll();
		revokedTokenRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void usersCrudShouldWorkAndWriteAuditRows() throws Exception {
		createUserDirect("admin", "admin@example.com", Role.ADMIN, "Password123!");
		String token = obtainToken("admin", "Password123!");

		String createResponse = mockMvc.perform(post("/users")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "new-user",
								  "email": "new-user@example.com",
								  "fullName": "New User",
								  "role": "DEVELOPER"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("new-user"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		Long userId = objectMapper.readTree(createResponse).get("id").asLong();

		mockMvc.perform(get("/users/{id}", userId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("new-user@example.com"));

		mockMvc.perform(post("/users/update/{id}", userId)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "fullName": "Updated User",
								  "role": "ADMIN"
								}
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/users/{id}", userId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		List<AuditLog> logs = auditLogRepository.findAll().stream()
				.filter(log -> "USER".equals(log.getEntityType()) && userId.equals(log.getEntityId()))
				.toList();
		assertEquals(3, logs.size());
	}

	@Test
	void projectTicketCommentFlowShouldEnforceRulesAndSoftDelete() throws Exception {
		createUserDirect("admin", "admin2@example.com", Role.ADMIN, "Password123!");
		createUserDirect("ownerUser", "owner@example.com", Role.DEVELOPER, "Password123!");
		createUserDirect("devUser", "dev@example.com", Role.DEVELOPER, "Password123!");
		String token = obtainToken("admin", "Password123!");

		Long ownerId = userRepository.findByUsername("ownerUser").orElseThrow().getId();
		Long assigneeId = userRepository.findByUsername("devUser").orElseThrow().getId();

		String projectResponse = mockMvc.perform(post("/projects")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Core Project",
								  "description": "Phase 3 project",
								  "ownerId": %d
								}
								""".formatted(ownerId)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		Long projectId = objectMapper.readTree(projectResponse).get("id").asLong();

		String ticketResponse = mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Core Ticket",
								  "description": "Ticket description",
								  "status": "TODO",
								  "priority": "LOW",
								  "type": "BUG",
								  "projectId": %d,
								  "assigneeId": %d
								}
								""".formatted(projectId, assigneeId)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		Long ticketId = objectMapper.readTree(ticketResponse).get("id").asLong();

		mockMvc.perform(patch("/tickets/{id}", ticketId)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "status": "DONE"
								}
								"""))
				.andExpect(status().isBadRequest());

		mockMvc.perform(patch("/tickets/{id}", ticketId)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "status": "IN_PROGRESS"
								}
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/tickets/{id}", ticketId)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "status": "IN_REVIEW"
								}
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/tickets/{id}", ticketId)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "status": "DONE"
								}
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/tickets/{id}", ticketId)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Should fail"
								}
								"""))
				.andExpect(status().isBadRequest());

		String commentResponse = mockMvc.perform(post("/tickets/{ticketId}/comments", ticketId)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "authorId": %d,
								  "content": "Hello @OWNERUSER"
								}
								""".formatted(ownerId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mentionedUsers[0].username").value("ownerUser"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		Long commentId = objectMapper.readTree(commentResponse).get("id").asLong();

		mockMvc.perform(patch("/tickets/{ticketId}/comments/{commentId}", ticketId, commentId)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "content": "Edited with no mention"
								}
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(get("/tickets/{ticketId}/comments", ticketId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].mentionedUsers").isArray())
				.andExpect(jsonPath("$[0].mentionedUsers.length()").value(0));

		mockMvc.perform(delete("/tickets/{ticketId}/comments/{commentId}", ticketId, commentId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/tickets/{id}", ticketId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(get("/tickets/{id}", ticketId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound());

		mockMvc.perform(delete("/projects/{id}", projectId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(get("/projects/{id}", projectId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound());

		List<AuditLog> logs = auditLogRepository.findAll();
		assertTrue(logs.stream().anyMatch(log -> "PROJECT".equals(log.getEntityType())));
		assertTrue(logs.stream().anyMatch(log -> "TICKET".equals(log.getEntityType())));
		assertTrue(logs.stream().anyMatch(log -> "COMMENT".equals(log.getEntityType())));
	}

	private void createUserDirect(String username, String email, Role role, String password) {
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setFullName(username + " full");
		user.setRole(role);
		user.setPasswordHash(passwordEncoder.encode(password));
		userRepository.saveAndFlush(user);
	}

	private String obtainToken(String username, String password) throws Exception {
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
		JsonNode json = objectMapper.readTree(response);
		return json.get("accessToken").asText();
	}

}
