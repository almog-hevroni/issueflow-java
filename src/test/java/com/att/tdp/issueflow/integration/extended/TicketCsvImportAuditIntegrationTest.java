package com.att.tdp.issueflow.integration.extended;

import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.audit.enums.AuditActorType;
import com.att.tdp.issueflow.user.enums.Role;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketCsvImportAuditIntegrationTest extends ExtendedFeaturesIntegrationTestSupport {

	@Test
	void csvImportShouldAuditEachSuccessfulRowOnly() throws Exception {
		createUserDirect("admin-csv", "admin-csv@example.com", Role.ADMIN, "Password123!");
		createUserDirect("owner-csv", "owner-csv@example.com", Role.DEVELOPER, "Password123!");
		createUserDirect("member-csv", "member-csv@example.com", Role.DEVELOPER, "Password123!");
		String adminToken = obtainToken("admin-csv", "Password123!");

		Long ownerId = userRepository.findByUsername("owner-csv").orElseThrow().getId();
		Long memberId = userRepository.findByUsername("member-csv").orElseThrow().getId();
		Long adminId = userRepository.findByUsername("admin-csv").orElseThrow().getId();
		Long projectId = createProject(adminToken, ownerId, "CSV Audit Project");

		String csvContent = "id,title,description,status,priority,type,assigneeId\n"
				+ "1,Good One,desc,TODO,LOW,BUG," + memberId + "\n"
				+ "2,Missing Member,desc,TODO,LOW,BUG,999999\n"
				+ "3,Good Two,desc,IN_PROGRESS,MEDIUM,FEATURE," + ownerId + "\n";
		MockMultipartFile importFile = new MockMultipartFile(
				"file",
				"tickets.csv",
				"text/csv",
				csvContent.getBytes(StandardCharsets.UTF_8));

		mockMvc.perform(multipart("/tickets/import")
						.file(importFile)
						.param("projectId", String.valueOf(projectId))
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.created").value(2))
				.andExpect(jsonPath("$.failed").value(1))
				.andExpect(jsonPath("$.errors.length()").value(1));

		var csvCreateLogs = auditLogRepository.findAll().stream()
				.filter(log -> log.getAction() == AuditAction.CREATE)
				.filter(log -> "TICKET".equals(log.getEntityType()))
				.filter(log -> log.getActorType() == AuditActorType.USER)
				.filter(log -> log.getActorUser() != null && adminId.equals(log.getActorUser().getId()))
				.filter(log -> log.getDetailsJson() != null && log.getDetailsJson().contains("\"source\":\"tickets-csv-import\""))
				.toList();

		assertEquals(2, csvCreateLogs.size());
		assertTrue(csvCreateLogs.stream().allMatch(log -> log.getDetailsJson().contains("\"projectId\":" + projectId)));
		assertTrue(projectMemberRepository.existsByProject_IdAndUser_Id(projectId, memberId));
	}
}
