package com.att.tdp.issueflow.common.exception;

import com.att.tdp.issueflow.common.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
			Exception exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		ApiErrorResponse response = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				exception.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(status).body(response);
	}
}
