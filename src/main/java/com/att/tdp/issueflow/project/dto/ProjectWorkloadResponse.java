package com.att.tdp.issueflow.project.dto;

public record ProjectWorkloadResponse(
		Long userId,
		String username,
		long openTicketCount
) {
}
