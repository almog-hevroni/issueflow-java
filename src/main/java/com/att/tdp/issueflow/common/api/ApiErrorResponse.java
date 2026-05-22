package com.att.tdp.issueflow.common.api;

import java.time.Instant;

public record ApiErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String message,
		String path
) {
}
