package com.att.tdp.issueflow.audit;

import com.att.tdp.issueflow.audit.dto.AuditLogResponse;
import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.audit.enums.AuditActorType;
import com.att.tdp.issueflow.audit.entity.AuditLog;
import com.att.tdp.issueflow.audit.repository.AuditLogRepository;
import com.att.tdp.issueflow.common.api.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit-logs")
@Tag(name = "Audit Logs", description = "Read-only audit log endpoints")
public class AuditController {

	private final AuditLogRepository auditLogRepository;

	public AuditController(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	@GetMapping
	@Operation(summary = "Get audit logs with optional filters")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid filter value",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
			@RequestParam(required = false) String entityType,
			@RequestParam(required = false) Long entityId,
			@RequestParam(required = false) AuditAction action,
			@RequestParam(required = false, name = "actor") AuditActorType actorType
	) {
		List<AuditLogResponse> response = auditLogRepository
				.search(entityType, entityId, action, actorType)
				.stream()
				.map(this::toResponse)
				.toList();
		return ResponseEntity.ok(response);
	}

	private AuditLogResponse toResponse(AuditLog log) {
		Long performedBy = log.getActorUser() != null ? log.getActorUser().getId() : null;
		return new AuditLogResponse(
				log.getId(),
				log.getAction(),
				log.getEntityType(),
				log.getEntityId(),
				performedBy,
				log.getActorType(),
				log.getDetailsJson(),
				log.getCreatedAt()
		);
	}
}
