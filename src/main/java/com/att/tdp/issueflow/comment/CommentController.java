package com.att.tdp.issueflow.comment;

import com.att.tdp.issueflow.comment.dto.CommentResponse;
import com.att.tdp.issueflow.comment.dto.CreateCommentRequest;
import com.att.tdp.issueflow.comment.dto.UpdateCommentRequest;
import com.att.tdp.issueflow.common.api.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tickets/{ticketId}/comments")
@Tag(name = "Comments", description = "Ticket comment management endpoints")
public class CommentController {

	private final CommentService commentService;

	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}

	@GetMapping
	@Operation(summary = "Get comments for a ticket")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Ticket not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long ticketId) {
		return ResponseEntity.ok(commentService.getCommentsByTicket(ticketId));
	}

	@PostMapping
	@Operation(summary = "Create a comment on a ticket")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Comment created successfully"),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid request body",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Ticket or author not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<CommentResponse> createComment(
			@PathVariable Long ticketId,
			@Valid @RequestBody CreateCommentRequest request
	) {
		return ResponseEntity.ok(commentService.createComment(ticketId, request));
	}

	@PatchMapping("/{commentId}")
	@Operation(summary = "Update a comment")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Comment updated successfully"),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid request body",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Ticket or comment not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "409",
					description = "Concurrent update conflict",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<Void> updateComment(
			@PathVariable Long ticketId,
			@PathVariable Long commentId,
			@Valid @RequestBody UpdateCommentRequest request
	) {
		commentService.updateComment(ticketId, commentId, request);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{commentId}")
	@Operation(summary = "Delete a comment")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Ticket or comment not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<Void> deleteComment(@PathVariable Long ticketId, @PathVariable Long commentId) {
		commentService.deleteComment(ticketId, commentId);
		return ResponseEntity.ok().build();
	}
}
