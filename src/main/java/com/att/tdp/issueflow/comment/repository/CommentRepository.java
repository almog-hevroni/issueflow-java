package com.att.tdp.issueflow.comment.repository;

import com.att.tdp.issueflow.comment.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findAllByTicket_IdOrderByCreatedAtDesc(Long ticketId);
}
