package com.att.tdp.issueflow.ticket.scheduler;

import com.att.tdp.issueflow.audit.AuditService;
import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.ticket.entity.Ticket;
import com.att.tdp.issueflow.ticket.enums.TicketPriority;
import com.att.tdp.issueflow.ticket.enums.TicketStatus;
import com.att.tdp.issueflow.ticket.repository.TicketRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TicketEscalationScheduler {

	private static final String ENTITY_TYPE = "TICKET";

	private final TicketRepository ticketRepository;
	private final AuditService auditService;
	private final boolean enabled;
	private final int batchSize;

	public TicketEscalationScheduler(
			TicketRepository ticketRepository,
			AuditService auditService,
			@Value("${issueflow.ticket.escalation.enabled:true}") boolean enabled,
			@Value("${issueflow.ticket.escalation.batch-size:100}") int batchSize
	) {
		this.ticketRepository = ticketRepository;
		this.auditService = auditService;
		this.enabled = enabled;
		this.batchSize = Math.max(1, batchSize);
	}

	@Scheduled(fixedDelayString = "${issueflow.ticket.escalation.fixed-delay-ms:60000}")
	@Transactional
	public void runEscalationCycle() {
		if (!enabled) {
			return;
		}

		List<Ticket> candidates = ticketRepository.findEscalationCandidates(Instant.now(), TicketStatus.DONE);
		int limit = Math.min(candidates.size(), batchSize);
		for (int i = 0; i < limit; i++) {
			Ticket ticket = candidates.get(i);
			EscalationChange change = applyEscalation(ticket);
			if (change.changed()) {
				ticketRepository.save(ticket);
				auditService.recordSystemAction(
						AuditAction.AUTO_ESCALATE,
						ENTITY_TYPE,
						ticket.getId(),
						"""
						{"source":"ticket-escalation","priorityBefore":"%s","priorityAfter":"%s","overdueBefore":%s,"overdueAfter":%s}
						""".formatted(
								change.priorityBefore(),
								ticket.getPriority(),
								change.overdueBefore(),
								ticket.isOverdue()));
			}
		}
	}

	private EscalationChange applyEscalation(Ticket ticket) {
		TicketPriority priorityBefore = ticket.getPriority();
		boolean overdueBefore = ticket.isOverdue();

		boolean changed = false;
		TicketPriority nextPriority = nextPriority(priorityBefore);
		if (nextPriority != priorityBefore) {
			ticket.setPriority(nextPriority);
			changed = true;
		} else if (!ticket.isOverdue()) {
			ticket.setOverdue(true);
			changed = true;
		}

		return new EscalationChange(changed, priorityBefore, overdueBefore);
	}

	private TicketPriority nextPriority(TicketPriority currentPriority) {
		return switch (currentPriority) {
			case LOW -> TicketPriority.MEDIUM;
			case MEDIUM -> TicketPriority.HIGH;
			case HIGH -> TicketPriority.CRITICAL;
			case CRITICAL -> TicketPriority.CRITICAL;
		};
	}

	private record EscalationChange(boolean changed, TicketPriority priorityBefore, boolean overdueBefore) {
	}
}
