package com.att.tdp.issueflow.ticket;

import com.att.tdp.issueflow.ticket.dto.CreateTicketRequest;
import com.att.tdp.issueflow.ticket.dto.TicketResponse;
import com.att.tdp.issueflow.ticket.dto.UpdateTicketRequest;
import com.att.tdp.issueflow.ticket.csv.TicketCsvService;
import com.att.tdp.issueflow.ticket.csv.TicketImportResultResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/tickets")
public class TicketController {

	private final TicketService ticketService;
	private final TicketCsvService ticketCsvService;

	public TicketController(TicketService ticketService, TicketCsvService ticketCsvService) {
		this.ticketService = ticketService;
		this.ticketCsvService = ticketCsvService;
	}

	@GetMapping
	public ResponseEntity<List<TicketResponse>> getTicketsByProject(@RequestParam Long projectId) {
		return ResponseEntity.ok(ticketService.getTicketsByProject(projectId));
	}

	@GetMapping("/{ticketId}")
	public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long ticketId) {
		return ResponseEntity.ok(ticketService.getTicketById(ticketId));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/deleted")
	public ResponseEntity<List<TicketResponse>> getDeletedTicketsByProject(@RequestParam Long projectId) {
		return ResponseEntity.ok(ticketService.getDeletedTicketsByProject(projectId));
	}

	@GetMapping("/export")
	public ResponseEntity<byte[]> exportTickets(@RequestParam Long projectId) {
		byte[] content = ticketCsvService.exportTickets(projectId);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tickets-" + projectId + ".csv\"")
				.contentType(MediaType.parseMediaType("text/csv"))
				.body(content);
	}

	@PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<TicketImportResultResponse> importTickets(
			@RequestParam("file") MultipartFile file,
			@RequestParam("projectId") Long projectId
	) {
		return ResponseEntity.ok(ticketCsvService.importTickets(projectId, file));
	}

	@PostMapping
	public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
		return ResponseEntity.ok(ticketService.createTicket(request));
	}

	@PatchMapping("/{ticketId}")
	public ResponseEntity<Void> updateTicket(@PathVariable Long ticketId, @Valid @RequestBody UpdateTicketRequest request) {
		ticketService.updateTicket(ticketId, request);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{ticketId}")
	public ResponseEntity<Void> deleteTicket(@PathVariable Long ticketId) {
		ticketService.deleteTicket(ticketId);
		return ResponseEntity.ok().build();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{ticketId}/restore")
	public ResponseEntity<Void> restoreTicket(@PathVariable Long ticketId) {
		ticketService.restoreTicket(ticketId);
		return ResponseEntity.ok().build();
	}
}
