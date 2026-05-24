package com.att.tdp.issueflow.ticket;

import com.att.tdp.issueflow.ticket.dto.CreateTicketRequest;
import com.att.tdp.issueflow.ticket.dto.TicketResponse;
import com.att.tdp.issueflow.ticket.dto.UpdateTicketRequest;
import com.att.tdp.issueflow.ticket.csv.TicketCsvService;
import com.att.tdp.issueflow.ticket.csv.TicketImportResultResponse;
import com.att.tdp.issueflow.common.api.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tickets", description = "Ticket management, import/export, and soft-delete endpoints")
public class TicketController {

	private final TicketService ticketService;
	private final TicketCsvService ticketCsvService;

	public TicketController(TicketService ticketService, TicketCsvService ticketCsvService) {
		this.ticketService = ticketService;
		this.ticketCsvService = ticketCsvService;
	}

	@GetMapping
	@Operation(summary = "Get tickets by project")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid projectId parameter",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<List<TicketResponse>> getTicketsByProject(@RequestParam Long projectId) {
		return ResponseEntity.ok(ticketService.getTicketsByProject(projectId));
	}

	@GetMapping("/{ticketId}")
	@Operation(summary = "Get ticket by ID")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Ticket retrieved successfully"),
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
	public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long ticketId) {
		return ResponseEntity.ok(ticketService.getTicketById(ticketId));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/deleted")
	@Operation(summary = "Get soft-deleted tickets by project (ADMIN)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Deleted tickets retrieved successfully"),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "403",
					description = "Forbidden",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<List<TicketResponse>> getDeletedTicketsByProject(@RequestParam Long projectId) {
		return ResponseEntity.ok(ticketService.getDeletedTicketsByProject(projectId));
	}

	@GetMapping("/export")
	@Operation(summary = "Export project tickets to CSV")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "CSV exported successfully"),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid projectId parameter",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Project not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<byte[]> exportTickets(@RequestParam Long projectId) {
		byte[] content = ticketCsvService.exportTickets(projectId);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tickets-" + projectId + ".csv\"")
				.contentType(MediaType.parseMediaType("text/csv"))
				.body(content);
	}

	@PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Import tickets from CSV into a project")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "CSV processed successfully"),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid file, projectId, or row content",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Project not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<TicketImportResultResponse> importTickets(
			@Parameter(description = "CSV file to import", required = true)
			@RequestParam("file") MultipartFile file,
			@Parameter(description = "Target project ID", required = true)
			@RequestParam("projectId") Long projectId
	) {
		return ResponseEntity.ok(ticketCsvService.importTickets(projectId, file));
	}

	@PostMapping
	@Operation(summary = "Create a ticket")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Ticket created successfully"),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid request body or business rule violation",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Project or assignee not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
		return ResponseEntity.ok(ticketService.createTicket(request));
	}

	@PatchMapping("/{ticketId}")
	@Operation(summary = "Update a ticket")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Ticket updated successfully"),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid request body or invalid state transition",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Ticket or assignee not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "409",
					description = "Concurrent update conflict",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<Void> updateTicket(@PathVariable Long ticketId, @Valid @RequestBody UpdateTicketRequest request) {
		ticketService.updateTicket(ticketId, request);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{ticketId}")
	@Operation(summary = "Soft-delete a ticket")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Ticket soft-deleted successfully"),
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
	public ResponseEntity<Void> deleteTicket(@PathVariable Long ticketId) {
		ticketService.deleteTicket(ticketId);
		return ResponseEntity.ok().build();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{ticketId}/restore")
	@Operation(summary = "Restore a soft-deleted ticket (ADMIN)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Ticket restored successfully"),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "403",
					description = "Forbidden",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			),
			@ApiResponse(
					responseCode = "404",
					description = "Ticket not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<Void> restoreTicket(@PathVariable Long ticketId) {
		ticketService.restoreTicket(ticketId);
		return ResponseEntity.ok().build();
	}
}
