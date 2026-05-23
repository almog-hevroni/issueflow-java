package com.att.tdp.issueflow.attachment;

import com.att.tdp.issueflow.attachment.dto.AttachmentResponse;
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
public class AttachmentController {

	private final AttachmentService attachmentService;

	public AttachmentController(AttachmentService attachmentService) {
		this.attachmentService = attachmentService;
	}

	@PostMapping
	public ResponseEntity<AttachmentResponse> uploadAttachment(
			@PathVariable Long ticketId,
			@RequestParam("file") MultipartFile file
	) {
		return ResponseEntity.ok(attachmentService.uploadAttachment(ticketId, file));
	}

	@DeleteMapping("/{attachmentId}")
	public ResponseEntity<Void> deleteAttachment(@PathVariable Long ticketId, @PathVariable Long attachmentId) {
		attachmentService.deleteAttachment(ticketId, attachmentId);
		return ResponseEntity.ok().build();
	}
}
