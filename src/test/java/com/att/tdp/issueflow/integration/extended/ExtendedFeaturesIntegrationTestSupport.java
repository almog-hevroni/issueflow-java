package com.att.tdp.issueflow.integration.extended;

import com.att.tdp.issueflow.attachment.repository.AttachmentRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
abstract class ExtendedFeaturesIntegrationTestSupport {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected PasswordEncoder passwordEncoder;

	@Autowired
	protected UserRepository userRepository;

	@Autowired
	protected ProjectRepository projectRepository;

	@Autowired
	protected TicketRepository ticketRepository;

	@Autowired
	protected CommentRepository commentRepository;

	@Autowired
	protected CommentMentionRepository commentMentionRepository;

	@Autowired
	protected TicketDependencyRepository ticketDependencyRepository;

	@Autowired
	protected AttachmentRepository attachmentRepository;

	@Autowired
	protected AuditLogRepository auditLogRepository;

	@Autowired
	protected RevokedTokenRepository revokedTokenRepository;

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

	protected Long createProject(String token, Long ownerId, String name) throws Exception {
		String projectResponse = mockMvc.perform(post("/projects")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "%s",
								  "description": "desc",
								  "ownerId": %d
								}
								""".formatted(name, ownerId)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return objectMapper.readTree(projectResponse).get("id").asLong();
	}

	protected Long createTicket(
			String token,
			Long projectId,
			String title,
			String statusValue,
			String priority,
			String type,
			Long assigneeId,
			String description
	) throws Exception {
		String assigneeField = assigneeId == null ? "" : """
				,"assigneeId": %d
				""".formatted(assigneeId);

		String ticketResponse = mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "%s",
								  "description": "%s",
								  "status": "%s",
								  "priority": "%s",
								  "type": "%s",
								  "projectId": %d
								  %s
								}
								""".formatted(title, description, statusValue, priority, type, projectId, assigneeField)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		JsonNode json = objectMapper.readTree(ticketResponse);
		return json.get("id").asLong();
	}

	protected void createUserDirect(String username, String email, Role role, String password) {
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setFullName(username + " full");
		user.setRole(role);
		user.setPasswordHash(passwordEncoder.encode(password));
		userRepository.saveAndFlush(user);
	}

	protected String obtainToken(String username, String password) throws Exception {
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
		return objectMapper.readTree(response).get("accessToken").asText();
	}
}
