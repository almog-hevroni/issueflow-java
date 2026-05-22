package com.att.tdp.issueflow.ticket.entity;

import com.att.tdp.issueflow.common.persistence.VersionedAuditableEntity;
import com.att.tdp.issueflow.project.entity.Project;
import com.att.tdp.issueflow.ticket.enums.TicketPriority;
import com.att.tdp.issueflow.ticket.enums.TicketStatus;
import com.att.tdp.issueflow.ticket.enums.TicketType;
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
import java.time.Instant;

@Entity
@Table(name = "tickets")
public class Ticket extends VersionedAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(name = "title", nullable = false, length = 255)
	private String title;

	@Column(name = "description", length = 4000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private TicketStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "priority", nullable = false, length = 20)
	private TicketPriority priority;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 20)
	private TicketType type;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignee_id")
	private User assignee;

	@Column(name = "due_date")
	private Instant dueDate;

	@Column(name = "is_overdue", nullable = false)
	private boolean overdue;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	public Long getId() {
		return id;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TicketStatus getStatus() {
		return status;
	}

	public void setStatus(TicketStatus status) {
		this.status = status;
	}

	public TicketPriority getPriority() {
		return priority;
	}

	public void setPriority(TicketPriority priority) {
		this.priority = priority;
	}

	public TicketType getType() {
		return type;
	}

	public void setType(TicketType type) {
		this.type = type;
	}

	public User getAssignee() {
		return assignee;
	}

	public void setAssignee(User assignee) {
		this.assignee = assignee;
	}

	public Instant getDueDate() {
		return dueDate;
	}

	public void setDueDate(Instant dueDate) {
		this.dueDate = dueDate;
	}

	public boolean isOverdue() {
		return overdue;
	}

	public void setOverdue(boolean overdue) {
		this.overdue = overdue;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(Instant deletedAt) {
		this.deletedAt = deletedAt;
	}
}
