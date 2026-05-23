package com.att.tdp.issueflow.ticket.dependency.dto;

import com.att.tdp.issueflow.ticket.enums.TicketStatus;

public record TicketDependencyResponse(
		Long id,
		String title,
		TicketStatus status
) {
}
