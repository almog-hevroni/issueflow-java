package com.att.tdp.issueflow.ticket.dependency.repository;

import com.att.tdp.issueflow.ticket.dependency.entity.TicketDependency;
import com.att.tdp.issueflow.ticket.dependency.entity.TicketDependencyId;
import com.att.tdp.issueflow.ticket.enums.TicketStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketDependencyRepository extends JpaRepository<TicketDependency, TicketDependencyId> {

	List<TicketDependency> findAllByTicket_Id(Long ticketId);

	boolean existsByTicket_IdAndBlockedByTicket_Id(Long ticketId, Long blockedByTicketId);

	boolean existsByTicket_IdAndBlockedByTicket_StatusNot(Long ticketId, TicketStatus status);

	long deleteByTicket_IdAndBlockedByTicket_Id(Long ticketId, Long blockedByTicketId);
}
