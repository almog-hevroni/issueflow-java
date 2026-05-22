package com.att.tdp.issueflow.comment.repository;

import com.att.tdp.issueflow.comment.entity.CommentMention;
import com.att.tdp.issueflow.comment.entity.CommentMentionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentMentionRepository extends JpaRepository<CommentMention, CommentMentionId> {

	List<CommentMention> findAllByMentionedUser_IdOrderByCreatedAtDesc(Long userId);

	List<CommentMention> findAllByComment_Id(Long commentId);
}
