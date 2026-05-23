package com.att.tdp.issueflow.audit;

import com.att.tdp.issueflow.audit.entity.AuditLog;
import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.audit.enums.AuditActorType;
import com.att.tdp.issueflow.audit.repository.AuditLogRepository;
import com.att.tdp.issueflow.security.auth.AuthUserDetails;
import com.att.tdp.issueflow.user.entity.User;
import com.att.tdp.issueflow.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

	private final AuditLogRepository auditLogRepository;
	private final UserRepository userRepository;

	public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
		this.auditLogRepository = auditLogRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public void recordUserAction(AuditAction action, String entityType, Long entityId, String detailsJson) {
		AuditLog log = new AuditLog();
		log.setAction(action);
		log.setEntityType(entityType);
		log.setEntityId(entityId);
		log.setDetailsJson(detailsJson);
		log.setActorType(AuditActorType.USER);
		log.setActorUser(resolveActorUser());
		auditLogRepository.save(log);
	}

	private User resolveActorUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserDetails userDetails)) {
			return null;
		}
		return userRepository.findById(userDetails.getId()).orElse(null);
	}
}
