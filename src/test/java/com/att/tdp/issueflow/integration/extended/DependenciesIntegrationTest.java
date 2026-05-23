package com.att.tdp.issueflow.integration.extended;

import com.att.tdp.issueflow.user.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DependenciesIntegrationTest extends ExtendedFeaturesIntegrationTestSupport {

	@Test
	void dependenciesShouldBlockDoneUntilBlockerIsResolved() throws Exception {
		createUserDirect("admin", "admin-phase4@example.com", Role.ADMIN, "Password123!");
		createUserDirect("dev-a", "dev-a@example.com", Role.DEVELOPER, "Password123!");
		String adminToken = obtainToken("admin", "Password123!");
		Long ownerId = userRepository.findByUsername("dev-a").orElseThrow().getId();

		Long projectId = createProject(adminToken, ownerId, "Dependencies Project");
		Long blockerId = createTicket(adminToken, projectId, "Blocker", "TODO", "LOW", "BUG", ownerId, "blocker desc");
		Long blockedId = createTicket(adminToken, projectId, "Blocked", "IN_REVIEW", "LOW", "BUG", ownerId, "blocked desc");

		mockMvc.perform(post("/tickets/{ticketId}/dependencies", blockedId)
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "blockedBy": %d }
								""".formatted(blockerId)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/tickets/{ticketId}/dependencies", blockedId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(blockerId));

		mockMvc.perform(patch("/tickets/{ticketId}", blockedId)
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "status": "DONE" }
								"""))
				.andExpect(status().isBadRequest());

		mockMvc.perform(patch("/tickets/{ticketId}", blockerId)
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "status": "IN_PROGRESS" }
								"""))
				.andExpect(status().isOk());
		mockMvc.perform(patch("/tickets/{ticketId}", blockerId)
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "status": "IN_REVIEW" }
								"""))
				.andExpect(status().isOk());
		mockMvc.perform(patch("/tickets/{ticketId}", blockerId)
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "status": "DONE" }
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/tickets/{ticketId}", blockedId)
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "status": "DONE" }
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/tickets/{ticketId}/dependencies/{blockerId}", blockedId, blockerId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk());
	}
}
