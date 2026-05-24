package com.att.tdp.issueflow.integration.extended;

import com.att.tdp.issueflow.audit.entity.AuditLog;
import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.audit.enums.AuditActorType;
import com.att.tdp.issueflow.user.enums.Role;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

		mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Seed Dev2 Membership",
								  "description": "seed",
								  "status": "TODO",
								  "priority": "LOW",
								  "type": "BUG",
								  "projectId": %d,
								  "assigneeId": %d
								}
								""".formatted(projectId, newDevId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.assigneeId").value(newDevId));

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
				.andExpect(jsonPath("$[*].userId", Matchers.containsInAnyOrder(oldDevId.intValue(), newDevId.intValue())))
				.andExpect(jsonPath("$[*].openTicketCount", Matchers.containsInAnyOrder(1, 1)));

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
	void explicitAssigneeShouldAutoLinkDeveloperToProject() throws Exception {
		createUserDirect("admin-assign", "admin-assign@example.com", Role.ADMIN, "Password123!");
		createUserDirect("owner-assign", "owner-assign@example.com", Role.DEVELOPER, "Password123!");
		createUserDirect("external-dev", "external-dev@example.com", Role.DEVELOPER, "Password123!");
		createUserDirect("admin-user", "admin-user@example.com", Role.ADMIN, "Password123!");
		String adminToken = obtainToken("admin-assign", "Password123!");

		Long ownerId = userRepository.findByUsername("owner-assign").orElseThrow().getId();
		Long externalDevId = userRepository.findByUsername("external-dev").orElseThrow().getId();
		Long adminUserId = userRepository.findByUsername("admin-user").orElseThrow().getId();
		Long projectId = createProject(adminToken, ownerId, "Assignee Rules Project");

		mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "External Dev Ticket",
								  "description": "ok",
								  "status": "TODO",
								  "priority": "LOW",
								  "type": "BUG",
								  "projectId": %d,
								  "assigneeId": %d
								}
								""".formatted(projectId, externalDevId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.assigneeId").value(externalDevId));

		mockMvc.perform(get("/projects/{projectId}/workload", projectId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.userId == %d)]".formatted(externalDevId)).exists());

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

	@Test
	void patchAssigneeShouldAutoLinkDeveloperToProject() throws Exception {
		createUserDirect("admin-patch", "admin-patch@example.com", Role.ADMIN, "Password123!");
		createUserDirect("owner-patch", "owner-patch@example.com", Role.DEVELOPER, "Password123!");
		createUserDirect("new-patch-dev", "new-patch-dev@example.com", Role.DEVELOPER, "Password123!");
		String adminToken = obtainToken("admin-patch", "Password123!");

		Long ownerId = userRepository.findByUsername("owner-patch").orElseThrow().getId();
		Long newDevId = userRepository.findByUsername("new-patch-dev").orElseThrow().getId();
		Long projectId = createProject(adminToken, ownerId, "Patch Membership Project");

		String createResponse = mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Patch Membership Ticket",
								  "description": "ticket",
								  "status": "TODO",
								  "priority": "LOW",
								  "type": "BUG",
								  "projectId": %d
								}
								""".formatted(projectId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.assigneeId").value(ownerId))
				.andReturn()
				.getResponse()
				.getContentAsString();
		Long ticketId = objectMapper.readTree(createResponse).get("id").asLong();

		mockMvc.perform(patch("/tickets/{ticketId}", ticketId)
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "assigneeId": %d
								}
								""".formatted(newDevId)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/projects/{projectId}/workload", projectId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.userId == %d)]".formatted(newDevId)).exists());
	}
}
