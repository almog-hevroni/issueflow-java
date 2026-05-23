package com.att.tdp.issueflow.user;

import com.att.tdp.issueflow.comment.dto.CommentResponse;
import com.att.tdp.issueflow.comment.dto.MentionedUserResponse;
import com.att.tdp.issueflow.comment.entity.Comment;
import com.att.tdp.issueflow.comment.entity.CommentMention;
import com.att.tdp.issueflow.comment.repository.CommentMentionRepository;
import com.att.tdp.issueflow.common.exception.NotFoundException;
import com.att.tdp.issueflow.user.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MentionService {

	private final CommentMentionRepository commentMentionRepository;
	private final UserRepository userRepository;

	public MentionService(CommentMentionRepository commentMentionRepository, UserRepository userRepository) {
		this.commentMentionRepository = commentMentionRepository;
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public List<CommentResponse> getMentionsForUser(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new NotFoundException("User not found: " + userId);
		}

		List<CommentMention> mentionsForUser = commentMentionRepository.findAllByMentionedUser_IdOrderByCreatedAtDesc(userId);
		List<Long> commentIds = mentionsForUser.stream()
				.map(CommentMention::getComment)
				.map(Comment::getId)
				.distinct()
				.toList();
		if (commentIds.isEmpty()) {
			return List.of();
		}

		Map<Long, List<MentionedUserResponse>> mentionsByCommentId = commentMentionRepository
				.findAllByComment_IdIn(commentIds)
				.stream()
				.collect(java.util.stream.Collectors.groupingBy(
						mention -> mention.getComment().getId(),
						LinkedHashMap::new,
						java.util.stream.Collectors.mapping(
								mention -> mention.getMentionedUser(),
								java.util.stream.Collectors.mapping(
										user -> new MentionedUserResponse(user.getId(), user.getUsername(), user.getFullName()),
										java.util.stream.Collectors.toList()
								)
						)
				));

		return mentionsForUser.stream()
				.map(CommentMention::getComment)
				.distinct()
				.map(comment -> new CommentResponse(
						comment.getId(),
						comment.getTicket().getId(),
						comment.getAuthor().getId(),
						comment.getContent(),
						mentionsByCommentId.getOrDefault(comment.getId(), List.of())))
				.toList();
	}
}
