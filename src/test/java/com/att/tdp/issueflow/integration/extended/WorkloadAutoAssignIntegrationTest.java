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
		linkProjectMember(projectId, newDevId);

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

	@Test
	void autoAssignmentShouldNotUseDevelopersOutsideProjectScope() throws Exception {
		createUserDirect("admin-scope", "admin-scope@example.com", Role.ADMIN, "Password123!");
		createUserDirect("owner-scope", "owner-scope@example.com", Role.DEVELOPER, "Password123!");
		createUserDirect("external-dev", "external-dev@example.com", Role.DEVELOPER, "Password123!");
		String adminToken = obtainToken("admin-scope", "Password123!");
		Long ownerId = userRepository.findByUsername("owner-scope").orElseThrow().getId();
		Long projectId = createProject(adminToken, ownerId, "Scoped Project");

		mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Scoped Auto Assign",
								  "description": "auto",
								  "status": "TODO",
								  "priority": "LOW",
								  "type": "BUG",
								  "projectId": %d
								}
								""".formatted(projectId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.assigneeId").value(ownerId));

		mockMvc.perform(get("/projects/{projectId}/workload", projectId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].userId").value(ownerId));
	}

	@Test
	void explicitAssigneeMustBeProjectLinkedDeveloper() throws Exception {
		createUserDirect("admin-assign", "admin-assign@example.com", Role.ADMIN, "Password123!");
		createUserDirect("owner-assign", "owner-assign@example.com", Role.DEVELOPER, "Password123!");
		createUserDirect("linked-dev", "linked-dev@example.com", Role.DEVELOPER, "Password123!");
		createUserDirect("external-dev", "external-dev@example.com", Role.DEVELOPER, "Password123!");
		createUserDirect("admin-user", "admin-user@example.com", Role.ADMIN, "Password123!");
		String adminToken = obtainToken("admin-assign", "Password123!");

		Long ownerId = userRepository.findByUsername("owner-assign").orElseThrow().getId();
		Long linkedDevId = userRepository.findByUsername("linked-dev").orElseThrow().getId();
		Long externalDevId = userRepository.findByUsername("external-dev").orElseThrow().getId();
		Long adminUserId = userRepository.findByUsername("admin-user").orElseThrow().getId();
		Long projectId = createProject(adminToken, ownerId, "Assignee Rules Project");
		linkProjectMember(projectId, linkedDevId);

		mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Linked Dev Ticket",
								  "description": "ok",
								  "status": "TODO",
								  "priority": "LOW",
								  "type": "BUG",
								  "projectId": %d,
								  "assigneeId": %d
								}
								""".formatted(projectId, linkedDevId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.assigneeId").value(linkedDevId));

		mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "External Dev Ticket",
								  "description": "fail",
								  "status": "TODO",
								  "priority": "LOW",
								  "type": "BUG",
								  "projectId": %d,
								  "assigneeId": %d
								}
								""".formatted(projectId, externalDevId)))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Admin Assignee Ticket",
								  "description": "fail",
								  "status": "TODO",
								  "priority": "LOW",
								  "type": "BUG",
								  "projectId": %d,
								  "assigneeId": %d
								}
								""".formatted(projectId, adminUserId)))
				.andExpect(status().isBadRequest());
	}
}
