package com.att.tdp.issueflow.project;

import com.att.tdp.issueflow.project.dto.CreateProjectRequest;
import com.att.tdp.issueflow.project.dto.ProjectResponse;
import com.att.tdp.issueflow.project.dto.ProjectWorkloadResponse;
import com.att.tdp.issueflow.project.dto.UpdateProjectRequest;
import com.att.tdp.issueflow.ticket.TicketService;
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
public class ProjectController {

	private final ProjectService projectService;
	private final TicketService ticketService;

	public ProjectController(ProjectService projectService, TicketService ticketService) {
		this.projectService = projectService;
		this.ticketService = ticketService;
	}

	@GetMapping
	public ResponseEntity<List<ProjectResponse>> getAllProjects() {
		return ResponseEntity.ok(projectService.getAllProjects());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/deleted")
	public ResponseEntity<List<ProjectResponse>> getDeletedProjects() {
		return ResponseEntity.ok(projectService.getDeletedProjects());
	}

	@GetMapping("/{projectId}")
	public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long projectId) {
		return ResponseEntity.ok(projectService.getProjectById(projectId));
	}

	@PostMapping
	public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
		return ResponseEntity.ok(projectService.createProject(request));
	}

	@PatchMapping("/{projectId}")
	public ResponseEntity<Void> updateProject(@PathVariable Long projectId, @Valid @RequestBody UpdateProjectRequest request) {
		projectService.updateProject(projectId, request);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{projectId}")
	public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
		projectService.deleteProject(projectId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{projectId}/workload")
	public ResponseEntity<List<ProjectWorkloadResponse>> getProjectWorkload(@PathVariable Long projectId) {
		return ResponseEntity.ok(ticketService.getWorkload(projectId));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{projectId}/restore")
	public ResponseEntity<Void> restoreProject(@PathVariable Long projectId) {
		projectService.restoreProject(projectId);
		return ResponseEntity.ok().build();
	}
}
