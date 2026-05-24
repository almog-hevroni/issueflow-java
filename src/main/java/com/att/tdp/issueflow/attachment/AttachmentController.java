package com.att.tdp.issueflow.attachment;

import com.att.tdp.issueflow.attachment.dto.AttachmentResponse;
import com.att.tdp.issueflow.common.api.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/tickets/{ticketId}/attachments")
@Tag(name = "Attachments", description = "Ticket attachment management endpoints")
public class AttachmentController {

	private final AttachmentService attachmentService;

	public AttachmentController(AttachmentService attachmentService) {
		this.attachmentService = attachmentService;
	}

	@PostMapping
	@Operation(summary = "Upload attachment to a ticket")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Attachment uploaded successfully"),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid file type/size or invalid request",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
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
	public ResponseEntity<AttachmentResponse> uploadAttachment(
			@PathVariable Long ticketId,
			@Parameter(description = "Attachment file", required = true)
			@RequestParam("file") MultipartFile file
	) {
		return ResponseEntity.ok(attachmentService.uploadAttachment(ticketId, file));
	}

	@DeleteMapping("/{attachmentId}")
	@Operation(summary = "Delete attachment from a ticket")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Attachment deleted successfully"),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Ticket or attachment not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<Void> deleteAttachment(@PathVariable Long ticketId, @PathVariable Long attachmentId) {
		attachmentService.deleteAttachment(ticketId, attachmentId);
		return ResponseEntity.ok().build();
	}
}
