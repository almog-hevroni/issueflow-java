package com.att.tdp.issueflow.user.dto;

import com.att.tdp.issueflow.comment.dto.CommentResponse;
import java.util.List;

public record UserMentionsResponse(
		List<CommentResponse> data,
		long total,
		int page
) {
}
