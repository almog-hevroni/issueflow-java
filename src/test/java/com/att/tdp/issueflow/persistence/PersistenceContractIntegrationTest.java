package com.att.tdp.issueflow.persistence;

import com.att.tdp.issueflow.comment.entity.Comment;
import com.att.tdp.issueflow.comment.entity.CommentMention;
import com.att.tdp.issueflow.comment.entity.CommentMentionId;
import com.att.tdp.issueflow.comment.repository.CommentMentionRepository;
import com.att.tdp.issueflow.comment.repository.CommentRepository;
import com.att.tdp.issueflow.project.entity.Project;
import com.att.tdp.issueflow.project.repository.ProjectRepository;
import com.att.tdp.issueflow.ticket.dependency.entity.TicketDependency;
import com.att.tdp.issueflow.ticket.dependency.entity.TicketDependencyId;
import com.att.tdp.issueflow.ticket.dependency.repository.TicketDependencyRepository;
import com.att.tdp.issueflow.ticket.entity.Ticket;
import com.att.tdp.issueflow.ticket.enums.TicketPriority;
import com.att.tdp.issueflow.ticket.enums.TicketStatus;
import com.att.tdp.issueflow.ticket.enums.TicketType;
import com.att.tdp.issueflow.ticket.repository.TicketRepository;
import com.att.tdp.issueflow.user.entity.User;
import com.att.tdp.issueflow.user.enums.Role;
import com.att.tdp.issueflow.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PersistenceContractIntegrationTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private TicketDependencyRepository ticketDependencyRepository;

	@Autowired
	private CommentMentionRepository commentMentionRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Test
	void flywaySchemaAndCorePersistenceShouldWork() {
		User owner = persistUser("owner", "owner@example.com", Role.ADMIN);
		User developer = persistUser("dev", "dev@example.com", Role.DEVELOPER);

		Project project = new Project();
		project.setName("IssueFlow");
		project.setDescription("Main project");
		project.setOwner(owner);
		project = projectRepository.saveAndFlush(project);

		Ticket ticket = new Ticket();
		ticket.setProject(project);
		ticket.setTitle("Ticket A");
		ticket.setDescription("Investigate");
		ticket.setStatus(TicketStatus.TODO);
		ticket.setPriority(TicketPriority.LOW);
		ticket.setType(TicketType.BUG);
		ticket.setAssignee(developer);
		ticket.setDueDate(Instant.now().plusSeconds(3600));
		ticket.setOverdue(false);
		ticket = ticketRepository.saveAndFlush(ticket);

		Comment comment = new Comment();
		comment.setTicket(ticket);
		comment.setAuthor(developer);
		comment.setContent("Please check this");
		comment = commentRepository.saveAndFlush(comment);

		CommentMention mention = new CommentMention();
		mention.setId(new CommentMentionId(comment.getId(), owner.getId()));
		mention.setComment(comment);
		mention.setMentionedUser(owner);
		mention.setCreatedAt(Instant.now());
		commentMentionRepository.saveAndFlush(mention);

		assertNotNull(project.getId());
		assertNotNull(ticket.getId());
		assertNotNull(comment.getId());

		List<Ticket> activeTickets = ticketRepository.findAllByProject_IdAndDeletedAtIsNull(project.getId());
		assertEquals(1, activeTickets.size());

		List<CommentMention> mentions = commentMentionRepository.findAllByMentionedUser_IdOrderByCreatedAtDesc(owner.getId());
		assertEquals(1, mentions.size());
	}

	@Test
	void uniqueUsernameConstraintShouldBeEnforced() {
		persistUser("duplicate", "dup1@example.com", Role.DEVELOPER);

		User sameUsername = new User();
		sameUsername.setUsername("duplicate");
		sameUsername.setEmail("dup2@example.com");
		sameUsername.setFullName("Dup User");
		sameUsername.setRole(Role.DEVELOPER);
		sameUsername.setPasswordHash("pwd");

		assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(sameUsername));
	}

	@Test
	void ticketSelfDependencyShouldBeRejected() {
		User owner = persistUser("owner2", "owner2@example.com", Role.ADMIN);
		Project project = new Project();
		project.setName("P2");
		project.setDescription("d");
		project.setOwner(owner);
		project = projectRepository.saveAndFlush(project);

		Ticket ticket = new Ticket();
		ticket.setProject(project);
		ticket.setTitle("Self dep");
		ticket.setDescription("desc");
		ticket.setStatus(TicketStatus.TODO);
		ticket.setPriority(TicketPriority.MEDIUM);
		ticket.setType(TicketType.FEATURE);
		ticket.setOverdue(false);
		ticket = ticketRepository.saveAndFlush(ticket);

		TicketDependency dependency = new TicketDependency();
		dependency.setId(new TicketDependencyId(ticket.getId(), ticket.getId()));
		dependency.setTicket(ticket);
		dependency.setBlockedByTicket(ticket);
		dependency.setCreatedAt(Instant.now());

		assertThrows(DataIntegrityViolationException.class, () -> ticketDependencyRepository.saveAndFlush(dependency));
	}

	@Test
	void optimisticLockShouldFailOnConcurrentTicketUpdate() {
		User owner = persistUser("owner3", "owner3@example.com", Role.ADMIN);
		Project project = new Project();
		project.setName("P3");
		project.setDescription("d");
		project.setOwner(owner);
		project = projectRepository.saveAndFlush(project);

		Ticket ticket = new Ticket();
		ticket.setProject(project);
		ticket.setTitle("Concurrent");
		ticket.setDescription("desc");
		ticket.setStatus(TicketStatus.TODO);
		ticket.setPriority(TicketPriority.HIGH);
		ticket.setType(TicketType.BUG);
		ticket.setOverdue(false);
		ticket = ticketRepository.saveAndFlush(ticket);
		final Long ticketId = ticket.getId();

		TransactionTemplate template = new TransactionTemplate(transactionManager);

		Ticket staleRead = template.execute(status ->
				ticketRepository.findById(ticketId).orElseThrow());

		template.executeWithoutResult(status -> {
			Ticket freshRead = ticketRepository.findById(ticketId).orElseThrow();
			freshRead.setTitle("First update");
			ticketRepository.saveAndFlush(freshRead);
		});

		assertThrows(ObjectOptimisticLockingFailureException.class, () ->
				template.executeWithoutResult(status -> {
					staleRead.setTitle("Second update");
					ticketRepository.saveAndFlush(staleRead);
				}));
	}

	@Test
	void softDeleteAwareRepositoryQueriesShouldExcludeDeletedRows() {
		User owner = persistUser("owner4", "owner4@example.com", Role.ADMIN);
		Project project = new Project();
		project.setName("P4");
		project.setDescription("d");
		project.setOwner(owner);
		project = projectRepository.saveAndFlush(project);

		Ticket active = new Ticket();
		active.setProject(project);
		active.setTitle("Active");
		active.setDescription("A");
		active.setStatus(TicketStatus.TODO);
		active.setPriority(TicketPriority.LOW);
		active.setType(TicketType.TECHNICAL);
		active.setOverdue(false);
		active = ticketRepository.saveAndFlush(active);

		Ticket deleted = new Ticket();
		deleted.setProject(project);
		deleted.setTitle("Deleted");
		deleted.setDescription("B");
		deleted.setStatus(TicketStatus.TODO);
		deleted.setPriority(TicketPriority.LOW);
		deleted.setType(TicketType.TECHNICAL);
		deleted.setOverdue(false);
		deleted.setDeletedAt(Instant.now());
		deleted = ticketRepository.saveAndFlush(deleted);

		List<Ticket> activeList = ticketRepository.findAllByProject_IdAndDeletedAtIsNull(project.getId());
		List<Ticket> deletedList = ticketRepository.findAllByProject_IdAndDeletedAtIsNotNull(project.getId());

		assertEquals(1, activeList.size());
		assertEquals(active.getId(), activeList.getFirst().getId());
		assertEquals(1, deletedList.size());
		assertEquals(deleted.getId(), deletedList.getFirst().getId());

		Optional<Ticket> byIdActive = ticketRepository.findByIdAndDeletedAtIsNull(active.getId());
		Optional<Ticket> byIdDeleted = ticketRepository.findByIdAndDeletedAtIsNull(deleted.getId());
		assertTrue(byIdActive.isPresent());
		assertTrue(byIdDeleted.isEmpty());
	}

	private User persistUser(String username, String email, Role role) {
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setFullName(username + " full");
		user.setRole(role);
		user.setPasswordHash("hashed");
		return userRepository.saveAndFlush(user);
	}
}
