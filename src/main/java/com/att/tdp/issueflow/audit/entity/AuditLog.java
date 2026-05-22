package com.att.tdp.issueflow.audit.entity;

import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.audit.enums.AuditActorType;
import com.att.tdp.issueflow.common.persistence.AuditableEntity;
import com.att.tdp.issueflow.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_logs")
public class AuditLog extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "actor_type", nullable = false, length = 20)
	private AuditActorType actorType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor_user_id")
	private User actorUser;

	@Enumerated(EnumType.STRING)
	@Column(name = "action", nullable = false, length = 50)
	private AuditAction action;

	@Column(name = "entity_type", nullable = false, length = 100)
	private String entityType;

	@Column(name = "entity_id", nullable = false)
	private Long entityId;

	@Column(name = "details_json", columnDefinition = "text")
	private String detailsJson;

	public Long getId() {
		return id;
	}

	public AuditActorType getActorType() {
		return actorType;
	}

	public void setActorType(AuditActorType actorType) {
		this.actorType = actorType;
	}

	public User getActorUser() {
		return actorUser;
	}

	public void setActorUser(User actorUser) {
		this.actorUser = actorUser;
	}

	public AuditAction getAction() {
		return action;
	}

	public void setAction(AuditAction action) {
		this.action = action;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public String getDetailsJson() {
		return detailsJson;
	}

	public void setDetailsJson(String detailsJson) {
		this.detailsJson = detailsJson;
	}
}
