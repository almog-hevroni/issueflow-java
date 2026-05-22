package com.att.tdp.issueflow.ticket.dependency.entity;

import com.att.tdp.issueflow.ticket.entity.Ticket;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ticket_dependencies")
public class TicketDependency {

	@EmbeddedId
	private TicketDependencyId id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("ticketId")
	@JoinColumn(name = "ticket_id", nullable = false)
	private Ticket ticket;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("blockedByTicketId")
	@JoinColumn(name = "blocked_by_ticket_id", nullable = false)
	private Ticket blockedByTicket;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public TicketDependencyId getId() {
		return id;
	}

	public void setId(TicketDependencyId id) {
		this.id = id;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}

	public Ticket getBlockedByTicket() {
		return blockedByTicket;
	}

	public void setBlockedByTicket(Ticket blockedByTicket) {
		this.blockedByTicket = blockedByTicket;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
