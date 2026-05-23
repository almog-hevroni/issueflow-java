package com.att.tdp.issueflow.ticket;

import com.att.tdp.issueflow.ticket.dto.CreateTicketRequest;
import com.att.tdp.issueflow.ticket.dto.TicketResponse;
import com.att.tdp.issueflow.ticket.dto.UpdateTicketRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tickets")
public class TicketController {

	private final TicketService ticketService;

	public TicketController(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	@GetMapping
	public ResponseEntity<List<TicketResponse>> getTicketsByProject(@RequestParam Long projectId) {
		return ResponseEntity.ok(ticketService.getTicketsByProject(projectId));
	}

	@GetMapping("/{ticketId}")
	public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long ticketId) {
		return ResponseEntity.ok(ticketService.getTicketById(ticketId));
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
}
