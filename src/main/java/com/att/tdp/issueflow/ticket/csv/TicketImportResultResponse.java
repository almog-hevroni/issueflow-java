package com.att.tdp.issueflow.ticket.csv;

import java.util.List;

public record TicketImportResultResponse(
		int created,
		int failed,
		List<String> errors
) {
}
