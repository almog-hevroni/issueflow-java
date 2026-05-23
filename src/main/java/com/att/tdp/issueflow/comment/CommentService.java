package com.att.tdp.issueflow.comment;

import com.att.tdp.issueflow.audit.AuditService;
import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.comment.dto.CommentResponse;
import com.att.tdp.issueflow.comment.dto.CreateCommentRequest;
import com.att.tdp.issueflow.comment.dto.MentionedUserResponse;
import com.att.tdp.issueflow.comment.dto.UpdateCommentRequest;
import com.att.tdp.issueflow.comment.entity.Comment;
import com.att.tdp.issueflow.comment.entity.CommentMention;
import com.att.tdp.issueflow.comment.entity.CommentMentionId;
import com.att.tdp.issueflow.comment.repository.CommentMentionRepository;
import com.att.tdp.issueflow.comment.repository.CommentRepository;
import com.att.tdp.issueflow.common.exception.NotFoundException;
import com.att.tdp.issueflow.ticket.entity.Ticket;
import com.att.tdp.issueflow.ticket.repository.TicketRepository;
import com.att.tdp.issueflow.user.entity.User;
import com.att.tdp.issueflow.user.repository.UserRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

	private static final Pattern MENTION_PATTERN = Pattern.compile("@([A-Za-z0-9._-]+)");
	private static final String ENTITY_TYPE = "COMMENT";

	private final CommentRepository commentRepository;
	private final CommentMentionRepository commentMentionRepository;
	private final TicketRepository ticketRepository;
	private final UserRepository userRepository;
	private final AuditService auditService;

	public CommentService(
			CommentRepository commentRepository,
			CommentMentionRepository commentMentionRepository,
			TicketRepository ticketRepository,
			UserRepository userRepository,
			AuditService auditService
	) {
		this.commentRepository = commentRepository;
		this.commentMentionRepository = commentMentionRepository;
		this.ticketRepository = ticketRepository;
		this.userRepository = userRepository;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public List<CommentResponse> getCommentsByTicket(Long ticketId) {
		ensureActiveTicket(ticketId);
		return commentRepository.findAllByTicket_IdOrderByCreatedAtDesc(ticketId).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public CommentResponse createComment(Long ticketId, CreateCommentRequest request) {
		Ticket ticket = ensureActiveTicket(ticketId);
		User author = userRepository.findById(request.authorId())
				.orElseThrow(() -> new NotFoundException("Author user not found: " + request.authorId()));

		Comment comment = new Comment();
		comment.setTicket(ticket);
		comment.setAuthor(author);
		comment.setContent(request.content());
		Comment saved = commentRepository.save(comment);
		syncMentions(saved);

		auditService.recordUserAction(AuditAction.CREATE, ENTITY_TYPE, saved.getId(), "{\"source\":\"comments-api\"}");
		return toResponse(saved);
	}

	@Transactional
	public void updateComment(Long ticketId, Long commentId, UpdateCommentRequest request) {
		ensureActiveTicket(ticketId);
		Comment comment = commentRepository.findByIdAndTicket_Id(commentId, ticketId)
				.orElseThrow(() -> new NotFoundException("Comment not found for ticket: " + commentId));
		comment.setContent(request.content());
		Comment saved = commentRepository.save(comment);
		syncMentions(saved);
		auditService.recordUserAction(AuditAction.UPDATE, ENTITY_TYPE, commentId, "{\"source\":\"comments-api\"}");
	}

	@Transactional
	public void deleteComment(Long ticketId, Long commentId) {
		ensureActiveTicket(ticketId);
		Comment comment = commentRepository.findByIdAndTicket_Id(commentId, ticketId)
				.orElseThrow(() -> new NotFoundException("Comment not found for ticket: " + commentId));
		commentMentionRepository.deleteAllByComment_Id(commentId);
		commentRepository.delete(comment);
		auditService.recordUserAction(AuditAction.DELETE, ENTITY_TYPE, commentId, "{\"source\":\"comments-api\"}");
	}

	private Ticket ensureActiveTicket(Long ticketId) {
		return ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
				.orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));
	}

	private void syncMentions(Comment comment) {
		commentMentionRepository.deleteAllByComment_Id(comment.getId());
		Matcher matcher = MENTION_PATTERN.matcher(comment.getContent());
		Set<Long> processedUserIds = new HashSet<>();
		while (matcher.find()) {
			String username = matcher.group(1);
			userRepository.findByUsernameIgnoreCase(username).ifPresent(mentionedUser -> {
				if (!processedUserIds.add(mentionedUser.getId())) {
					return;
				}
				CommentMention mention = new CommentMention();
				mention.setId(new CommentMentionId(comment.getId(), mentionedUser.getId()));
				mention.setComment(comment);
				mention.setMentionedUser(mentionedUser);
				mention.setCreatedAt(Instant.now());
				commentMentionRepository.save(mention);
			});
		}
	}

	private CommentResponse toResponse(Comment comment) {
		List<MentionedUserResponse> mentions = commentMentionRepository.findAllByComment_Id(comment.getId()).stream()
				.map(CommentMention::getMentionedUser)
				.map(u -> new MentionedUserResponse(u.getId(), u.getUsername(), u.getFullName()))
				.toList();
		return new CommentResponse(
				comment.getId(),
				comment.getTicket().getId(),
				comment.getAuthor().getId(),
				comment.getContent(),
				mentions
		);
	}
}
