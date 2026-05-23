package com.att.tdp.issueflow.comment.repository;

import com.att.tdp.issueflow.comment.entity.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findAllByTicket_IdOrderByCreatedAtDesc(Long ticketId);

	Optional<Comment> findByIdAndTicket_Id(Long commentId, Long ticketId);
}
