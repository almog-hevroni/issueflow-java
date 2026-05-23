package com.att.tdp.issueflow.ticket.dependency;

import com.att.tdp.issueflow.common.exception.BadRequestException;
import com.att.tdp.issueflow.common.exception.NotFoundException;
import com.att.tdp.issueflow.ticket.dependency.dto.AddDependencyRequest;
import com.att.tdp.issueflow.ticket.dependency.dto.TicketDependencyResponse;
import com.att.tdp.issueflow.ticket.dependency.entity.TicketDependency;
import com.att.tdp.issueflow.ticket.dependency.entity.TicketDependencyId;
import com.att.tdp.issueflow.ticket.dependency.repository.TicketDependencyRepository;
import com.att.tdp.issueflow.ticket.entity.Ticket;
import com.att.tdp.issueflow.ticket.repository.TicketRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketDependencyService {

	private final TicketRepository ticketRepository;
	private final TicketDependencyRepository ticketDependencyRepository;

	public TicketDependencyService(TicketRepository ticketRepository, TicketDependencyRepository ticketDependencyRepository) {
		this.ticketRepository = ticketRepository;
		this.ticketDependencyRepository = ticketDependencyRepository;
	}

	@Transactional
	public void addDependency(Long ticketId, AddDependencyRequest request) {
		Long blockerId = request.blockedBy();
		if (ticketId.equals(blockerId)) {
			throw new BadRequestException("Ticket cannot depend on itself");
		}

		Ticket ticket = findActiveTicket(ticketId);
		Ticket blocker = findActiveTicket(blockerId);
		if (!ticket.getProject().getId().equals(blocker.getProject().getId())) {
			throw new BadRequestException("Dependencies are allowed only between tickets in the same project");
		}

		boolean exists = ticketDependencyRepository.existsByTicket_IdAndBlockedByTicket_Id(ticketId, blockerId);
		if (exists) {
			throw new BadRequestException("Dependency already exists");
		}

		TicketDependency dependency = new TicketDependency();
		dependency.setId(new TicketDependencyId(ticketId, blockerId));
		dependency.setTicket(ticket);
		dependency.setBlockedByTicket(blocker);
		dependency.setCreatedAt(Instant.now());
		ticketDependencyRepository.save(dependency);
	}

	@Transactional(readOnly = true)
	public List<TicketDependencyResponse> getDependencies(Long ticketId) {
		findActiveTicket(ticketId);
		return ticketDependencyRepository.findAllByTicket_Id(ticketId).stream()
				.map(TicketDependency::getBlockedByTicket)
				.map(blocker -> new TicketDependencyResponse(
						blocker.getId(),
						blocker.getTitle(),
						blocker.getStatus()))
				.toList();
	}

	@Transactional
	public void removeDependency(Long ticketId, Long blockerId) {
		findActiveTicket(ticketId);
		findActiveTicket(blockerId);
		long removed = ticketDependencyRepository.deleteByTicket_IdAndBlockedByTicket_Id(ticketId, blockerId);
		if (removed == 0) {
			throw new NotFoundException("Dependency not found");
		}
	}

	private Ticket findActiveTicket(Long ticketId) {
		return ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
				.orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));
	}
}
