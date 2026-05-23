package com.att.tdp.issueflow.integration.extended;

import com.att.tdp.issueflow.audit.entity.AuditLog;
import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.audit.enums.AuditActorType;
import com.att.tdp.issueflow.user.enums.Role;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkloadAutoAssignIntegrationTest extends ExtendedFeaturesIntegrationTestSupport {

	@Test
	void workloadAndAutoAssignmentShouldUseLeastLoadedDeveloper() throws Exception {
		createUserDirect("admin", "admin-workload@example.com", Role.ADMIN, "Password123!");
		createUserDirect("dev-old", "dev-old@example.com", Role.DEVELOPER, "Password123!");
		createUserDirect("dev-new", "dev-new@example.com", Role.DEVELOPER, "Password123!");
		String adminToken = obtainToken("admin", "Password123!");

		Long ownerId = userRepository.findByUsername("dev-old").orElseThrow().getId();
		Long oldDevId = ownerId;
		Long newDevId = userRepository.findByUsername("dev-new").orElseThrow().getId();
		Long projectId = createProject(adminToken, ownerId, "Workload Project");

		String createResponse = mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Auto Assigned",
								  "description": "auto",
								  "status": "TODO",
								  "priority": "LOW",
								  "type": "BUG",
								  "projectId": %d
								}
								""".formatted(projectId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.assigneeId").value(oldDevId))
				.andReturn()
				.getResponse()
				.getContentAsString();
		Long ticketId = objectMapper.readTree(createResponse).get("id").asLong();

		mockMvc.perform(get("/projects/{projectId}/workload", projectId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].userId").value(newDevId))
				.andExpect(jsonPath("$[0].openTicketCount").value(0))
				.andExpect(jsonPath("$[1].userId").value(oldDevId))
				.andExpect(jsonPath("$[1].openTicketCount").value(1));

		List<AuditLog> autoAssignLogs = auditLogRepository.findAll().stream()
				.filter(log -> log.getAction() == AuditAction.AUTO_ASSIGN && ticketId.equals(log.getEntityId()))
				.toList();
		assertEquals(1, autoAssignLogs.size());
		assertEquals(AuditActorType.SYSTEM, autoAssignLogs.get(0).getActorType());
	}
}
