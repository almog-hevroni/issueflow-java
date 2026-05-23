package com.att.tdp.issueflow.project;

import com.att.tdp.issueflow.audit.AuditService;
import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.common.exception.BadRequestException;
import com.att.tdp.issueflow.common.exception.NotFoundException;
import com.att.tdp.issueflow.project.dto.CreateProjectRequest;
import com.att.tdp.issueflow.project.dto.ProjectResponse;
import com.att.tdp.issueflow.project.dto.UpdateProjectRequest;
import com.att.tdp.issueflow.project.entity.Project;
import com.att.tdp.issueflow.project.repository.ProjectRepository;
import com.att.tdp.issueflow.user.entity.User;
import com.att.tdp.issueflow.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

	private static final String ENTITY_TYPE = "PROJECT";

	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final AuditService auditService;

	public ProjectService(
			ProjectRepository projectRepository,
			UserRepository userRepository,
			AuditService auditService
	) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public List<ProjectResponse> getAllProjects() {
		return projectRepository.findAllByDeletedAtIsNull().stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public ProjectResponse getProjectById(Long projectId) {
		Project project = findActiveProject(projectId);
		return toResponse(project);
	}

	@Transactional
	public ProjectResponse createProject(CreateProjectRequest request) {
		User owner = userRepository.findById(request.ownerId())
				.orElseThrow(() -> new NotFoundException("Owner user not found: " + request.ownerId()));

		Project project = new Project();
		project.setName(request.name());
		project.setDescription(request.description());
		project.setOwner(owner);

		Project saved = projectRepository.save(project);
		auditService.recordUserAction(AuditAction.CREATE, ENTITY_TYPE, saved.getId(), "{\"source\":\"projects-api\"}");
		return toResponse(saved);
	}

	@Transactional
	public void updateProject(Long projectId, UpdateProjectRequest request) {
		Project project = findActiveProject(projectId);
		if (request.name() == null && request.description() == null) {
			throw new BadRequestException("At least one updatable field is required");
		}
		if (request.name() != null) {
			project.setName(request.name());
		}
		if (request.description() != null) {
			project.setDescription(request.description());
		}
		projectRepository.save(project);
		auditService.recordUserAction(AuditAction.UPDATE, ENTITY_TYPE, projectId, "{\"source\":\"projects-api\"}");
	}

	@Transactional
	public void deleteProject(Long projectId) {
		Project project = findActiveProject(projectId);
		project.setDeletedAt(Instant.now());
		projectRepository.save(project);
		auditService.recordUserAction(AuditAction.DELETE, ENTITY_TYPE, projectId, "{\"source\":\"projects-api\"}");
	}

	private Project findActiveProject(Long projectId) {
		return projectRepository.findByIdAndDeletedAtIsNull(projectId)
				.orElseThrow(() -> new NotFoundException("Project not found: " + projectId));
	}

	private ProjectResponse toResponse(Project project) {
		return new ProjectResponse(
				project.getId(),
				project.getName(),
				project.getDescription(),
				project.getOwner().getId()
		);
	}
}
