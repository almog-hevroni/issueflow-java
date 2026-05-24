package com.att.tdp.issueflow.audit.dto;

import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.audit.enums.AuditActorType;
import java.time.Instant;

public record AuditLogResponse(
		Long id,
		AuditAction action,
		String entityType,
		Long entityId,
		Long performedBy,
		AuditActorType actor,
		String detailsJson,
		Instant timestamp
) {
}
