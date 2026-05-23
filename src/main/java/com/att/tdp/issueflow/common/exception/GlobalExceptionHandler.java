package com.att.tdp.issueflow.common.exception;

import com.att.tdp.issueflow.common.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
			AuthenticationException exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.UNAUTHORIZED;
		return ResponseEntity.status(status).body(buildResponse(status, exception.getMessage(), request));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
			AccessDeniedException exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.FORBIDDEN;
		return ResponseEntity.status(status).body(buildResponse(status, exception.getMessage(), request));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationException(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String message = exception.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.orElse("Validation failed");
		return ResponseEntity.status(status).body(buildResponse(status, message, request));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadableMessageException(
			HttpMessageNotReadableException exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String message = "Malformed request body";
		Throwable cause = exception.getMostSpecificCause();
		if (cause instanceof ConversionFailedException conversionFailedException
				&& conversionFailedException.getTargetType() != null
				&& conversionFailedException.getTargetType().getType().getTypeName().endsWith(".Role")) {
			message = "Invalid request body: role must be one of [ADMIN, DEVELOPER]";
		} else if (cause != null && cause.getMessage() != null && cause.getMessage().contains("Role")) {
			message = "Invalid request body: role must be one of [ADMIN, DEVELOPER]";
		}
		return ResponseEntity.status(status).body(buildResponse(status, message, request));
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
			ResponseStatusException exception,
			HttpServletRequest request
	) {
		HttpStatusCode statusCode = exception.getStatusCode();
		String message = exception.getReason() != null ? exception.getReason() : exception.getMessage();
		ApiErrorResponse response = new ApiErrorResponse(
				Instant.now(),
				statusCode.value(),
				HttpStatus.valueOf(statusCode.value()).getReasonPhrase(),
				message,
				request.getRequestURI()
		);
		return ResponseEntity.status(statusCode).body(response);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFoundException(
			NotFoundException exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.NOT_FOUND;
		return ResponseEntity.status(status).body(buildResponse(status, exception.getMessage(), request));
	}

	@ExceptionHandler({
			BadRequestException.class,
			InvalidStateTransitionException.class,
			ImmutableTicketException.class
	})
	public ResponseEntity<ApiErrorResponse> handleBadRequestException(
			RuntimeException exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status).body(buildResponse(status, exception.getMessage(), request));
	}

	@ExceptionHandler({
			ConflictException.class,
			ObjectOptimisticLockingFailureException.class,
			DataIntegrityViolationException.class
	})
	public ResponseEntity<ApiErrorResponse> handleConflictException(
			Exception exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.CONFLICT;
		String message = exception instanceof ObjectOptimisticLockingFailureException
				? "Concurrent update detected. Please refresh and retry."
				: exception.getMessage();
		return ResponseEntity.status(status).body(buildResponse(status, message, request));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
			Exception exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		return ResponseEntity.status(status).body(buildResponse(status, exception.getMessage(), request));
	}

	private ApiErrorResponse buildResponse(HttpStatus status, String message, HttpServletRequest request) {
		return new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI()
		);
	}
}
