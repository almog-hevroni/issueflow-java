package com.att.tdp.issueflow.comment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CommentMentionId implements Serializable {

	@Column(name = "comment_id", nullable = false)
	private Long commentId;

	@Column(name = "mentioned_user_id", nullable = false)
	private Long mentionedUserId;

	public CommentMentionId() {
	}

	public CommentMentionId(Long commentId, Long mentionedUserId) {
		this.commentId = commentId;
		this.mentionedUserId = mentionedUserId;
	}

	public Long getCommentId() {
		return commentId;
	}

	public Long getMentionedUserId() {
		return mentionedUserId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CommentMentionId that)) {
			return false;
		}
		return Objects.equals(commentId, that.commentId) && Objects.equals(mentionedUserId, that.mentionedUserId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(commentId, mentionedUserId);
	}
}
