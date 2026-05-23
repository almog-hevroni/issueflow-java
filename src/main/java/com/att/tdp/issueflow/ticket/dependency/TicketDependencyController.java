package com.att.tdp.issueflow.ticket.dependency;

import com.att.tdp.issueflow.ticket.dependency.dto.AddDependencyRequest;
import com.att.tdp.issueflow.ticket.dependency.dto.TicketDependencyResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tickets/{ticketId}/dependencies")
public class TicketDependencyController {

	private final TicketDependencyService dependencyService;

	public TicketDependencyController(TicketDependencyService dependencyService) {
		this.dependencyService = dependencyService;
	}

	@PostMapping
	public ResponseEntity<Void> addDependency(@PathVariable Long ticketId, @Valid @RequestBody AddDependencyRequest request) {
		dependencyService.addDependency(ticketId, request);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	public ResponseEntity<List<TicketDependencyResponse>> getDependencies(@PathVariable Long ticketId) {
		return ResponseEntity.ok(dependencyService.getDependencies(ticketId));
	}

	@DeleteMapping("/{blockerId}")
	public ResponseEntity<Void> removeDependency(@PathVariable Long ticketId, @PathVariable Long blockerId) {
		dependencyService.removeDependency(ticketId, blockerId);
		return ResponseEntity.ok().build();
	}
}
