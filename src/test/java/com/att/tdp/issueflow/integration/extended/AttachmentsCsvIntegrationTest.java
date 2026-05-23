package com.att.tdp.issueflow.integration.extended;

import com.att.tdp.issueflow.user.enums.Role;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AttachmentsCsvIntegrationTest extends ExtendedFeaturesIntegrationTestSupport {

	@Test
	void attachmentsAndCsvShouldSupportValidationAndRoundTrip() throws Exception {
		createUserDirect("admin", "admin-files@example.com", Role.ADMIN, "Password123!");
		createUserDirect("owner", "owner-files@example.com", Role.DEVELOPER, "Password123!");
		String adminToken = obtainToken("admin", "Password123!");
		Long ownerId = userRepository.findByUsername("owner").orElseThrow().getId();
		Long projectId = createProject(adminToken, ownerId, "Files Project");
		Long ticketId = createTicket(adminToken, projectId, "Ticket A,1", "TODO", "LOW", "BUG", ownerId, "desc,comma");

		MockMultipartFile goodFile = new MockMultipartFile(
				"file",
				"note.txt",
				"text/plain",
				"sample".getBytes(StandardCharsets.UTF_8));
		String uploadResponse = mockMvc.perform(multipart("/tickets/{ticketId}/attachments", ticketId)
						.file(goodFile)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.contentType").value("text/plain"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		Long attachmentId = objectMapper.readTree(uploadResponse).get("id").asLong();

		MockMultipartFile badTypeFile = new MockMultipartFile(
				"file",
				"bad.json",
				"application/json",
				"{}".getBytes(StandardCharsets.UTF_8));
		mockMvc.perform(multipart("/tickets/{ticketId}/attachments", ticketId)
						.file(badTypeFile)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isBadRequest());

		mockMvc.perform(delete("/tickets/{ticketId}/attachments/{attachmentId}", ticketId, attachmentId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk());

		mockMvc.perform(get("/tickets/export")
						.param("projectId", String.valueOf(projectId))
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("tickets-" + projectId + ".csv")))
				.andExpect(content().contentType("text/csv"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("\"Ticket A,1\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("\"desc,comma\"")));

		String importCsv = "id,title,description,status,priority,type,assigneeId\n"
				+ "1,\"Imported, Ticket\",\"quoted \"\"text\"\"\",TODO,LOW,BUG," + ownerId + "\n"
				+ "2,Bad Ticket,desc,NOT_A_STATUS,LOW,BUG," + ownerId + "\n";
		MockMultipartFile importFile = new MockMultipartFile(
				"file",
				"tickets.csv",
				"text/csv",
				importCsv.getBytes(StandardCharsets.UTF_8));

		mockMvc.perform(multipart("/tickets/import")
						.file(importFile)
						.param("projectId", String.valueOf(projectId))
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.created").value(1))
				.andExpect(jsonPath("$.failed").value(1))
				.andExpect(jsonPath("$.errors[0]").exists());

		assertTrue(ticketRepository.findAllByProject_IdAndDeletedAtIsNull(projectId).size() >= 2);
	}
}
