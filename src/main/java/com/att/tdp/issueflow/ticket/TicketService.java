package com.att.tdp.issueflow.ticket;

import com.att.tdp.issueflow.audit.AuditService;
import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.common.exception.BadRequestException;
import com.att.tdp.issueflow.common.exception.ImmutableTicketException;
import com.att.tdp.issueflow.common.exception.InvalidStateTransitionException;
import com.att.tdp.issueflow.common.exception.NotFoundException;
import com.att.tdp.issueflow.project.entity.Project;
import com.att.tdp.issueflow.project.repository.ProjectRepository;
import com.att.tdp.issueflow.ticket.dto.CreateTicketRequest;
import com.att.tdp.issueflow.ticket.dto.TicketResponse;
import com.att.tdp.issueflow.ticket.dto.UpdateTicketRequest;
import com.att.tdp.issueflow.ticket.entity.Ticket;
import com.att.tdp.issueflow.ticket.enums.TicketStatus;
import com.att.tdp.issueflow.ticket.repository.TicketRepository;
import com.att.tdp.issueflow.user.entity.User;
import com.att.tdp.issueflow.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

	private static final String ENTITY_TYPE = "TICKET";

	private final TicketRepository ticketRepository;
	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final AuditService auditService;

	public TicketService(
			TicketRepository ticketRepository,
			ProjectRepository projectRepository,
			UserRepository userRepository,
			AuditService auditService
	) {
		this.ticketRepository = ticketRepository;
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public List<TicketResponse> getTicketsByProject(Long projectId) {
		if (!projectRepository.existsByIdAndDeletedAtIsNull(projectId)) {
			throw new NotFoundException("Project not found: " + projectId);
		}
		return ticketRepository.findAllByProject_IdAndDeletedAtIsNull(projectId).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public TicketResponse getTicketById(Long ticketId) {
		return toResponse(findActiveTicket(ticketId));
	}

	@Transactional
	public TicketResponse createTicket(CreateTicketRequest request) {
		Project project = projectRepository.findByIdAndDeletedAtIsNull(request.projectId())
				.orElseThrow(() -> new NotFoundException("Project not found: " + request.projectId()));

		Ticket ticket = new Ticket();
		ticket.setProject(project);
		ticket.setTitle(request.title());
		ticket.setDescription(request.description());
		ticket.setStatus(request.status());
		ticket.setPriority(request.priority());
		ticket.setType(request.type());
		ticket.setDueDate(request.dueDate());
		ticket.setOverdue(false);
		ticket.setAssignee(resolveAssignee(request.assigneeId()));

		Ticket saved = ticketRepository.save(ticket);
		auditService.recordUserAction(AuditAction.CREATE, ENTITY_TYPE, saved.getId(), "{\"source\":\"tickets-api\"}");
		return toResponse(saved);
	}

	@Transactional
	public void updateTicket(Long ticketId, UpdateTicketRequest request) {
		Ticket ticket = findActiveTicket(ticketId);
		boolean hasChange = request.title() != null
				|| request.description() != null
				|| request.status() != null
				|| request.priority() != null
				|| request.assigneeId() != null
				|| request.dueDate() != null;
		if (!hasChange) {
			throw new BadRequestException("At least one updatable field is required");
		}
		if (ticket.getStatus() == TicketStatus.DONE) {
			throw new ImmutableTicketException("DONE tickets cannot be updated");
		}
		if (request.status() != null) {
			validateStatusTransition(ticket.getStatus(), request.status());
			ticket.setStatus(request.status());
		}
		if (request.title() != null) {
			ticket.setTitle(request.title());
		}
		if (request.description() != null) {
			ticket.setDescription(request.description());
		}
		if (request.priority() != null) {
			ticket.setPriority(request.priority());
		}
		if (request.dueDate() != null) {
			ticket.setDueDate(request.dueDate());
		}
		if (request.assigneeId() != null) {
			ticket.setAssignee(resolveAssignee(request.assigneeId()));
		}
		ticketRepository.save(ticket);
		auditService.recordUserAction(AuditAction.UPDATE, ENTITY_TYPE, ticketId, "{\"source\":\"tickets-api\"}");
	}

	@Transactional
	public void deleteTicket(Long ticketId) {
		Ticket ticket = findActiveTicket(ticketId);
		ticket.setDeletedAt(Instant.now());
		ticketRepository.save(ticket);
		auditService.recordUserAction(AuditAction.DELETE, ENTITY_TYPE, ticketId, "{\"source\":\"tickets-api\"}");
	}

	private Ticket findActiveTicket(Long ticketId) {
		return ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
				.orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));
	}

	private User resolveAssignee(Long assigneeId) {
		if (assigneeId == null) {
			return null;
		}
		return userRepository.findById(assigneeId)
				.orElseThrow(() -> new NotFoundException("Assignee user not found: " + assigneeId));
	}

	private void validateStatusTransition(TicketStatus currentStatus, TicketStatus newStatus) {
		if (currentStatus == newStatus) {
			return;
		}
		if (newStatus.ordinal() < currentStatus.ordinal()) {
			throw new InvalidStateTransitionException(
					"Invalid status transition from " + currentStatus + " to " + newStatus);
		}
		if (newStatus.ordinal() - currentStatus.ordinal() > 1) {
			throw new BadRequestException(
					"Status transition must advance one step at a time from " + currentStatus + " to " + newStatus);
		}
	}

	private TicketResponse toResponse(Ticket ticket) {
		Long assigneeId = ticket.getAssignee() != null ? ticket.getAssignee().getId() : null;
		return new TicketResponse(
				ticket.getId(),
				ticket.getTitle(),
				ticket.getDescription(),
				ticket.getStatus(),
				ticket.getPriority(),
				ticket.getType(),
				ticket.getProject().getId(),
				assigneeId,
				ticket.getDueDate(),
				ticket.isOverdue()
		);
	}
}
