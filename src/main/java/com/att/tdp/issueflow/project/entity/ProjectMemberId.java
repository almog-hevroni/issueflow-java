package com.att.tdp.issueflow.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProjectMemberId implements Serializable {

	@Column(name = "project_id", nullable = false)
	private Long projectId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	public ProjectMemberId() {
	}

	public ProjectMemberId(Long projectId, Long userId) {
		this.projectId = projectId;
		this.userId = userId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public Long getUserId() {
		return userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ProjectMemberId that)) {
			return false;
		}
		return Objects.equals(projectId, that.projectId) && Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, userId);
	}
}
