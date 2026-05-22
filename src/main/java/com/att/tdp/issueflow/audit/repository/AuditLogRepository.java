package com.att.tdp.issueflow.audit.repository;

import com.att.tdp.issueflow.audit.entity.AuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

	List<AuditLog> findAllByEntityTypeOrderByCreatedAtDesc(String entityType);

	List<AuditLog> findAllByActorUser_IdOrderByCreatedAtDesc(Long actorUserId);
}
