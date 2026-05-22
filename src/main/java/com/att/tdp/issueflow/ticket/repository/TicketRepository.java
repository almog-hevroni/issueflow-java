package com.att.tdp.issueflow.ticket.repository;

import com.att.tdp.issueflow.ticket.entity.Ticket;
import com.att.tdp.issueflow.ticket.enums.TicketStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	Optional<Ticket> findByIdAndDeletedAtIsNull(Long id);

	List<Ticket> findAllByProject_IdAndDeletedAtIsNull(Long projectId);

	List<Ticket> findAllByProject_IdAndDeletedAtIsNotNull(Long projectId);

	@Query("""
			select count(t)
			from Ticket t
			where t.project.id = :projectId
			  and t.assignee.id = :assigneeId
			  and t.deletedAt is null
			  and t.status <> :doneStatus
			""")
	long countOpenAssignedTickets(@Param("projectId") Long projectId,
								  @Param("assigneeId") Long assigneeId,
								  @Param("doneStatus") TicketStatus doneStatus);
}
