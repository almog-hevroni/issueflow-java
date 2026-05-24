package com.att.tdp.issueflow.project;

import com.att.tdp.issueflow.project.dto.CreateProjectRequest;
import com.att.tdp.issueflow.project.dto.ProjectResponse;
import com.att.tdp.issueflow.project.dto.ProjectWorkloadResponse;
import com.att.tdp.issueflow.project.dto.UpdateProjectRequest;
import com.att.tdp.issueflow.ticket.TicketService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
@Tag(name = "Projects", description = "Project management and workload endpoints")
public class ProjectController {

	private final ProjectService projectService;
	private final TicketService ticketService;

	public ProjectController(ProjectService projectService, TicketService ticketService) {
		this.projectService = projectService;
		this.ticketService = ticketService;
	}

	@GetMapping
	@Operation(summary = "Get all active projects")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<List<ProjectResponse>> getAllProjects() {
		return ResponseEntity.ok(projectService.getAllProjects());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/deleted")
	@Operation(summary = "Get soft-deleted projects (ADMIN)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Deleted projects retrieved successfully"),
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
	public ResponseEntity<List<ProjectResponse>> getDeletedProjects() {
		return ResponseEntity.ok(projectService.getDeletedProjects());
	}

	@GetMapping("/{projectId}")
	@Operation(summary = "Get project by ID")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Project retrieved successfully"),
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
	public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long projectId) {
		return ResponseEntity.ok(projectService.getProjectById(projectId));
	}

	@PostMapping
	@Operation(summary = "Create a project")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Project created successfully"),
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
					description = "Owner user not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
		return ResponseEntity.ok(projectService.createProject(request));
	}

	@PatchMapping("/{projectId}")
	@Operation(summary = "Update a project")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Project updated successfully"),
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
					description = "Project not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<Void> updateProject(@PathVariable Long projectId, @Valid @RequestBody UpdateProjectRequest request) {
		projectService.updateProject(projectId, request);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{projectId}")
	@Operation(summary = "Soft-delete a project")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Project soft-deleted successfully"),
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
	public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
		projectService.deleteProject(projectId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{projectId}/workload")
	@Operation(summary = "Get project workload")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Workload retrieved successfully"),
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
	public ResponseEntity<List<ProjectWorkloadResponse>> getProjectWorkload(@PathVariable Long projectId) {
		return ResponseEntity.ok(ticketService.getWorkload(projectId));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{projectId}/restore")
	@Operation(summary = "Restore a soft-deleted project (ADMIN)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Project restored successfully"),
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
					description = "Project not found",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
			)
	})
	public ResponseEntity<Void> restoreProject(@PathVariable Long projectId) {
		projectService.restoreProject(projectId);
		return ResponseEntity.ok().build();
	}
}
