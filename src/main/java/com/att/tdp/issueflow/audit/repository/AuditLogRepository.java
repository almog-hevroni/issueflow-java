package com.att.tdp.issueflow.audit.repository;

import com.att.tdp.issueflow.audit.entity.AuditLog;
import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.audit.enums.AuditActorType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

	List<AuditLog> findAllByEntityTypeOrderByCreatedAtDesc(String entityType);

	List<AuditLog> findAllByActorUser_IdOrderByCreatedAtDesc(Long actorUserId);

	@Query("""
			select a
			from AuditLog a
			where (:entityType is null or a.entityType = :entityType)
			  and (:entityId is null or a.entityId = :entityId)
			  and (:action is null or a.action = :action)
			  and (:actorType is null or a.actorType = :actorType)
			order by a.createdAt desc
			""")
	List<AuditLog> search(
			@Param("entityType") String entityType,
			@Param("entityId") Long entityId,
			@Param("action") AuditAction action,
			@Param("actorType") AuditActorType actorType
	);
}
