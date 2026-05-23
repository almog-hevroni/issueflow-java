package com.att.tdp.issueflow.integration.extended;

import com.att.tdp.issueflow.user.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MentionsIntegrationTest extends ExtendedFeaturesIntegrationTestSupport {

	@Test
	void mentionsEndpointShouldReturnNewestFirst() throws Exception {
		createUserDirect("admin", "admin-mentions@example.com", Role.ADMIN, "Password123!");
		createUserDirect("owner", "owner-mentions@example.com", Role.DEVELOPER, "Password123!");
		createUserDirect("mentioned", "mentioned@example.com", Role.DEVELOPER, "Password123!");
		String adminToken = obtainToken("admin", "Password123!");
		Long ownerId = userRepository.findByUsername("owner").orElseThrow().getId();
		Long mentionedId = userRepository.findByUsername("mentioned").orElseThrow().getId();

		Long projectId = createProject(adminToken, ownerId, "Mentions Project");
		Long ticketId = createTicket(adminToken, projectId, "Mentions Ticket", "TODO", "LOW", "BUG", ownerId, "desc");

		mockMvc.perform(post("/tickets/{ticketId}/comments", ticketId)
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "authorId": %d, "content": "first @mentioned" }
								""".formatted(ownerId)))
				.andExpect(status().isOk());

		mockMvc.perform(post("/tickets/{ticketId}/comments", ticketId)
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "authorId": %d, "content": "second @mentioned" }
								""".formatted(ownerId)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/users/{userId}/mentions", mentionedId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].content").value("second @mentioned"))
				.andExpect(jsonPath("$[1].content").value("first @mentioned"));
	}
}
