package com.att.tdp.issueflow.ticket.dependency;

import com.att.tdp.issueflow.ticket.dependency.dto.AddDependencyRequest;
import com.att.tdp.issueflow.ticket.dependency.dto.TicketDependencyResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tickets/{ticketId}/dependencies")
@Tag(name = "Dependencies", description = "Ticket dependency management endpoints")
public class TicketDependencyController {

	private final TicketDependencyService dependencyService;

	public TicketDependencyController(TicketDependencyService dependencyService) {
		this.dependencyService = dependencyService;
	}

	@PostMapping
	@Operation(summary = "Add dependency to a ticket")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Dependency added successfully"),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid request or dependency rule violation",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Ticket or blocker not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "409",
					description = "Dependency already exists",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<Void> addDependency(@PathVariable Long ticketId, @Valid @RequestBody AddDependencyRequest request) {
		dependencyService.addDependency(ticketId, request);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	@Operation(summary = "List ticket dependencies")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Dependencies retrieved successfully"),
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
	public ResponseEntity<List<TicketDependencyResponse>> getDependencies(@PathVariable Long ticketId) {
		return ResponseEntity.ok(dependencyService.getDependencies(ticketId));
	}

	@DeleteMapping("/{blockerId}")
	@Operation(summary = "Remove dependency from a ticket")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Dependency removed successfully"),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Ticket or dependency not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<Void> removeDependency(@PathVariable Long ticketId, @PathVariable Long blockerId) {
		dependencyService.removeDependency(ticketId, blockerId);
		return ResponseEntity.ok().build();
	}
}
