package com.att.tdp.issueflow.ticket.dto;

import com.att.tdp.issueflow.ticket.enums.TicketPriority;
import com.att.tdp.issueflow.ticket.enums.TicketStatus;
import com.att.tdp.issueflow.ticket.enums.TicketType;
import java.time.Instant;

public record TicketResponse(
		Long id,
		String title,
		String description,
		TicketStatus status,
		TicketPriority priority,
		TicketType type,
		Long projectId,
		Long assigneeId,
		Instant dueDate,
		boolean isOverdue
) {
}
