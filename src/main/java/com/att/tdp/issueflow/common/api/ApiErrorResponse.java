package com.att.tdp.issueflow.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Standard API error response")
public record ApiErrorResponse(
		@Schema(description = "Timestamp when the error was generated", example = "2026-05-23T18:15:30Z")
		Instant timestamp,
		@Schema(description = "HTTP status code", example = "400")
		int status,
		@Schema(description = "HTTP status reason phrase", example = "Bad Request")
		String error,
		@Schema(description = "Human-readable error message", example = "Validation failed")
		String message,
		@Schema(description = "Request path that caused the error", example = "/tickets/1")
		String path
) {
}
