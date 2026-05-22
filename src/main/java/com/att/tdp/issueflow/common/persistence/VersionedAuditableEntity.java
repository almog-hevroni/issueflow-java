package com.att.tdp.issueflow.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
public abstract class VersionedAuditableEntity extends AuditableEntity {

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	public Long getVersion() {
		return version;
	}
}
