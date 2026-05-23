package com.att.tdp.issueflow.integration.extended;

import com.att.tdp.issueflow.audit.entity.AuditLog;
import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.audit.enums.AuditActorType;
import com.att.tdp.issueflow.ticket.scheduler.TicketEscalationScheduler;
import com.att.tdp.issueflow.user.enums.Role;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "issueflow.ticket.escalation.enabled=true")
class EscalationSchedulerIntegrationTest extends ExtendedFeaturesIntegrationTestSupport {

	@Autowired
	private TicketEscalationScheduler ticketEscalationScheduler;

	@Test
	void schedulerShouldEscalateOverdueTicketsAndWriteSystemAudit() throws Exception {
		createUserDirect("admin", "admin-phase5@example.com", Role.ADMIN, "Password123!");
		createUserDirect("dev-phase5", "dev-phase5@example.com", Role.DEVELOPER, "Password123!");
		String token = obtainToken("admin", "Password123!");
		Long ownerId = userRepository.findByUsername("dev-phase5").orElseThrow().getId();
		Long projectId = createProject(token, ownerId, "Escalation Project");

		Instant overdueDate = Instant.now().minus(2, ChronoUnit.DAYS);
		String lowTicketResponse = mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Overdue Low",
								  "description": "phase5",
								  "status": "TODO",
								  "priority": "LOW",
								  "type": "BUG",
								  "projectId": %d,
								  "assigneeId": %d,
								  "dueDate": "%s"
								}
								""".formatted(projectId, ownerId, overdueDate.toString())))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		Long lowTicketId = objectMapper.readTree(lowTicketResponse).get("id").asLong();

		String criticalTicketResponse = mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Overdue Critical",
								  "description": "phase5",
								  "status": "IN_PROGRESS",
								  "priority": "CRITICAL",
								  "type": "BUG",
								  "projectId": %d,
								  "assigneeId": %d,
								  "dueDate": "%s"
								}
								""".formatted(projectId, ownerId, overdueDate.toString())))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		Long criticalTicketId = objectMapper.readTree(criticalTicketResponse).get("id").asLong();

		String withoutDueDateResponse = mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "No Due Date",
								  "description": "phase5",
								  "status": "TODO",
								  "priority": "LOW",
								  "type": "BUG",
								  "projectId": %d,
								  "assigneeId": %d
								}
								""".formatted(projectId, ownerId)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		Long noDueDateTicketId = objectMapper.readTree(withoutDueDateResponse).get("id").asLong();

		String doneTicketResponse = mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Done Ticket",
								  "description": "phase5",
								  "status": "DONE",
								  "priority": "LOW",
								  "type": "BUG",
								  "projectId": %d,
								  "assigneeId": %d,
								  "dueDate": "%s"
								}
								""".formatted(projectId, ownerId, overdueDate.toString())))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		Long doneTicketId = objectMapper.readTree(doneTicketResponse).get("id").asLong();

		String deletedTicketResponse = mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Deleted Ticket",
								  "description": "phase5",
								  "status": "TODO",
								  "priority": "LOW",
								  "type": "BUG",
								  "projectId": %d,
								  "assigneeId": %d,
								  "dueDate": "%s"
								}
								""".formatted(projectId, ownerId, overdueDate.toString())))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		Long deletedTicketId = objectMapper.readTree(deletedTicketResponse).get("id").asLong();
		mockMvc.perform(delete("/tickets/{ticketId}", deletedTicketId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		ticketEscalationScheduler.runEscalationCycle();

		mockMvc.perform(get("/tickets/{ticketId}", lowTicketId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.priority").value("MEDIUM"))
				.andExpect(jsonPath("$.isOverdue").value(false));

		mockMvc.perform(get("/tickets/{ticketId}", criticalTicketId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.priority").value("CRITICAL"))
				.andExpect(jsonPath("$.isOverdue").value(true));

		mockMvc.perform(get("/tickets/{ticketId}", noDueDateTicketId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.priority").value("LOW"))
				.andExpect(jsonPath("$.isOverdue").value(false));

		mockMvc.perform(get("/tickets/{ticketId}", doneTicketId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.priority").value("LOW"))
				.andExpect(jsonPath("$.isOverdue").value(false));

		List<AuditLog> escalationLogs = auditLogRepository.findAll().stream()
				.filter(log -> log.getAction() == AuditAction.AUTO_ESCALATE)
				.toList();
		assertEquals(2, escalationLogs.size());
		assertEquals(2, escalationLogs.stream()
				.filter(log -> log.getActorType() == AuditActorType.SYSTEM)
				.count());
	}

	@Test
	void manualPriorityChangeShouldResetOverdueAndNextCycleShouldEscalateFromNewPriority() throws Exception {
		createUserDirect("admin-reset", "admin-reset@example.com", Role.ADMIN, "Password123!");
		createUserDirect("dev-reset", "dev-reset@example.com", Role.DEVELOPER, "Password123!");
		String token = obtainToken("admin-reset", "Password123!");
		Long ownerId = userRepository.findByUsername("dev-reset").orElseThrow().getId();
		Long projectId = createProject(token, ownerId, "Escalation Reset Project");
		Instant overdueDate = Instant.now().minus(1, ChronoUnit.DAYS);

		String ticketResponse = mockMvc.perform(post("/tickets")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Reset Candidate",
								  "description": "phase5",
								  "status": "IN_PROGRESS",
								  "priority": "HIGH",
								  "type": "BUG",
								  "projectId": %d,
								  "assigneeId": %d,
								  "dueDate": "%s"
								}
								""".formatted(projectId, ownerId, overdueDate.toString())))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		Long ticketId = objectMapper.readTree(ticketResponse).get("id").asLong();

		ticketEscalationScheduler.runEscalationCycle();
		ticketEscalationScheduler.runEscalationCycle();

		mockMvc.perform(get("/tickets/{ticketId}", ticketId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.priority").value("CRITICAL"))
				.andExpect(jsonPath("$.isOverdue").value(true));

		mockMvc.perform(patch("/tickets/{ticketId}", ticketId)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "priority": "MEDIUM"
								}
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(get("/tickets/{ticketId}", ticketId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.priority").value("MEDIUM"))
				.andExpect(jsonPath("$.isOverdue").value(false));

		ticketEscalationScheduler.runEscalationCycle();

		mockMvc.perform(get("/tickets/{ticketId}", ticketId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.priority").value("HIGH"))
				.andExpect(jsonPath("$.isOverdue").value(false));
	}
}
