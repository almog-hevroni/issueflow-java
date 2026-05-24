package com.att.tdp.issueflow.integration.extended;

import com.att.tdp.issueflow.user.enums.Role;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditLogsIntegrationTest extends ExtendedFeaturesIntegrationTestSupport {

	@Test
	void auditLogsEndpointShouldReturnFilteredData() throws Exception {
		createUserDirect("admin", "admin-audit@example.com", Role.ADMIN, "Password123!");
		createUserDirect("owner-audit", "owner-audit@example.com", Role.DEVELOPER, "Password123!");
		String adminToken = obtainToken("admin", "Password123!");
		Long ownerId = userRepository.findByUsername("owner-audit").orElseThrow().getId();
		Long projectId = createProject(adminToken, ownerId, "Audit Project");
		Long ticketId = createTicket(adminToken, projectId, "Audit Ticket", "TODO", "LOW", "BUG", ownerId, "desc");

		mockMvc.perform(get("/audit-logs")
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").exists());

		mockMvc.perform(get("/audit-logs")
						.header("Authorization", "Bearer " + adminToken)
						.param("entityType", "TICKET")
						.param("entityId", String.valueOf(ticketId))
						.param("action", "CREATE")
						.param("actor", "USER"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].entityType").value("TICKET"))
				.andExpect(jsonPath("$[0].entityId").value(ticketId))
				.andExpect(jsonPath("$[0].action").value("CREATE"))
				.andExpect(jsonPath("$[0].actor").value("USER"));
	}

	@Test
	void loginAndLogoutShouldBeRecordedInAudit() throws Exception {
		createUserDirect("admin-auth-audit", "admin-auth-audit@example.com", Role.ADMIN, "Password123!");
		createUserDirect("audited-user", "audited-user@example.com", Role.DEVELOPER, "Password123!");
		String adminToken = obtainToken("admin-auth-audit", "Password123!");
		String token = obtainToken("audited-user", "Password123!");

		mockMvc.perform(post("/auth/logout")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(get("/audit-logs")
						.header("Authorization", "Bearer " + adminToken)
						.param("entityType", "AUTH")
						.param("action", "LOGIN"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].entityType").value("AUTH"))
				.andExpect(jsonPath("$[0].action").value("LOGIN"));

		mockMvc.perform(get("/audit-logs")
						.header("Authorization", "Bearer " + adminToken)
						.param("entityType", "AUTH")
						.param("action", "LOGOUT"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].entityType").value("AUTH"))
				.andExpect(jsonPath("$[0].action").value("LOGOUT"));
	}
}
