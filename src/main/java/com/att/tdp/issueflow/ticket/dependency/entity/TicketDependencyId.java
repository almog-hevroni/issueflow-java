package com.att.tdp.issueflow.ticket.dependency.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TicketDependencyId implements Serializable {

	@Column(name = "ticket_id", nullable = false)
	private Long ticketId;

	@Column(name = "blocked_by_ticket_id", nullable = false)
	private Long blockedByTicketId;

	public TicketDependencyId() {
	}

	public TicketDependencyId(Long ticketId, Long blockedByTicketId) {
		this.ticketId = ticketId;
		this.blockedByTicketId = blockedByTicketId;
	}

	public Long getTicketId() {
		return ticketId;
	}

	public Long getBlockedByTicketId() {
		return blockedByTicketId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TicketDependencyId that)) {
			return false;
		}
		return Objects.equals(ticketId, that.ticketId) && Objects.equals(blockedByTicketId, that.blockedByTicketId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ticketId, blockedByTicketId);
	}
}
