package com.att.tdp.issueflow.comment.entity;

import com.att.tdp.issueflow.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "comment_mentions")
public class CommentMention {

	@EmbeddedId
	private CommentMentionId id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("commentId")
	@JoinColumn(name = "comment_id", nullable = false)
	private Comment comment;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("mentionedUserId")
	@JoinColumn(name = "mentioned_user_id", nullable = false)
	private User mentionedUser;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public CommentMentionId getId() {
		return id;
	}

	public void setId(CommentMentionId id) {
		this.id = id;
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public User getMentionedUser() {
		return mentionedUser;
	}

	public void setMentionedUser(User mentionedUser) {
		this.mentionedUser = mentionedUser;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
