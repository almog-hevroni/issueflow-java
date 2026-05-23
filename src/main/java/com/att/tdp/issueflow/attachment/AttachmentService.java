package com.att.tdp.issueflow.attachment;

import com.att.tdp.issueflow.attachment.dto.AttachmentResponse;
import com.att.tdp.issueflow.attachment.entity.Attachment;
import com.att.tdp.issueflow.attachment.repository.AttachmentRepository;
import com.att.tdp.issueflow.common.exception.BadRequestException;
import com.att.tdp.issueflow.common.exception.NotFoundException;
import com.att.tdp.issueflow.security.auth.AuthUserDetails;
import com.att.tdp.issueflow.ticket.entity.Ticket;
import com.att.tdp.issueflow.ticket.repository.TicketRepository;
import com.att.tdp.issueflow.user.entity.User;
import com.att.tdp.issueflow.user.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentService {

	private static final long MAX_SIZE_BYTES = 10L * 1024L * 1024L;
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			"image/png",
			"image/jpeg",
			"application/pdf",
			"text/plain");

	private final AttachmentRepository attachmentRepository;
	private final TicketRepository ticketRepository;
	private final UserRepository userRepository;
	private final Path attachmentRoot;

	public AttachmentService(
			AttachmentRepository attachmentRepository,
			TicketRepository ticketRepository,
			UserRepository userRepository,
			@Value("${issueflow.attachments.storage-root:build/attachments}") String attachmentRoot
	) {
		this.attachmentRepository = attachmentRepository;
		this.ticketRepository = ticketRepository;
		this.userRepository = userRepository;
		this.attachmentRoot = Paths.get(attachmentRoot).toAbsolutePath().normalize();
	}

	@Transactional
	public AttachmentResponse uploadAttachment(Long ticketId, MultipartFile file) {
		Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
				.orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));
		validateFile(file);
		User actor = resolveCurrentUser();

		String originalFileName = sanitizeFileName(file.getOriginalFilename());
		String storedName = UUID.randomUUID() + "-" + originalFileName;
		Path ticketDirectory = attachmentRoot.resolve("ticket-" + ticketId);
		Path targetPath = ticketDirectory.resolve(storedName).normalize();

		try {
			Files.createDirectories(ticketDirectory);
			Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to store attachment", exception);
		}

		Attachment attachment = new Attachment();
		attachment.setTicket(ticket);
		attachment.setUploadedBy(actor);
		attachment.setFileName(originalFileName);
		attachment.setContentType(file.getContentType());
		attachment.setSizeBytes(file.getSize());
		attachment.setStoragePath(targetPath.toString());
		Attachment saved = attachmentRepository.save(attachment);
		return new AttachmentResponse(saved.getId(), ticketId, saved.getFileName(), saved.getContentType());
	}

	@Transactional
	public void deleteAttachment(Long ticketId, Long attachmentId) {
		Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
				.orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));

		Attachment attachment = attachmentRepository.findById(attachmentId)
				.orElseThrow(() -> new NotFoundException("Attachment not found: " + attachmentId));
		if (!attachment.getTicket().getId().equals(ticket.getId())) {
			throw new NotFoundException("Attachment not found: " + attachmentId);
		}

		try {
			Files.deleteIfExists(Paths.get(attachment.getStoragePath()));
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to delete attachment content", exception);
		}
		attachmentRepository.delete(attachment);
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("Attachment file is required");
		}
		if (file.getSize() > MAX_SIZE_BYTES) {
			throw new BadRequestException("Attachment exceeds maximum allowed size of 10MB");
		}
		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new BadRequestException("Unsupported attachment content type");
		}
	}

	private User resolveCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserDetails principal)) {
			throw new NotFoundException("Authenticated user not found");
		}
		return userRepository.findById(principal.getId())
				.orElseThrow(() -> new NotFoundException("Authenticated user not found"));
	}

	private String sanitizeFileName(String value) {
		String fileName = value == null || value.isBlank() ? "attachment.bin" : value;
		return fileName.replaceAll("[\\\\/]+", "_");
	}
}
