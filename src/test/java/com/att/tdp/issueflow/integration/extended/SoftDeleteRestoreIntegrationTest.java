package com.att.tdp.issueflow.integration.extended;

import com.att.tdp.issueflow.audit.entity.AuditLog;
import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.user.enums.Role;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SoftDeleteRestoreIntegrationTest extends ExtendedFeaturesIntegrationTestSupport {

	@Test
	void adminOnlyDeletedAndRestoreEndpointsShouldBeProtected() throws Exception {
		createUserDirect("admin", "admin-restore@example.com", Role.ADMIN, "Password123!");
		createUserDirect("dev", "dev-restore@example.com", Role.DEVELOPER, "Password123!");
		String adminToken = obtainToken("admin", "Password123!");
		String devToken = obtainToken("dev", "Password123!");
		Long ownerId = userRepository.findByUsername("dev").orElseThrow().getId();

		Long projectId = createProject(adminToken, ownerId, "Restore Project");
		Long ticketId = createTicket(adminToken, projectId, "Restore Ticket", "TODO", "LOW", "BUG", ownerId, "desc");

		mockMvc.perform(delete("/tickets/{ticketId}", ticketId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk());
		mockMvc.perform(delete("/projects/{projectId}", projectId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk());

		mockMvc.perform(get("/tickets/deleted")
						.param("projectId", String.valueOf(projectId))
						.header("Authorization", "Bearer " + devToken))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/projects/deleted")
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(projectId));

		mockMvc.perform(post("/tickets/{ticketId}/restore", ticketId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk());
		mockMvc.perform(post("/projects/{projectId}/restore", projectId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk());

		List<AuditLog> restoreLogs = auditLogRepository.findAll().stream()
				.filter(log -> log.getAction() == AuditAction.RESTORE)
				.toList();
		assertEquals(2, restoreLogs.size());
	}
}
