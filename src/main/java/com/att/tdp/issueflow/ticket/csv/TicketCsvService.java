package com.att.tdp.issueflow.ticket.csv;

import com.att.tdp.issueflow.audit.AuditService;
import com.att.tdp.issueflow.audit.enums.AuditAction;
import com.att.tdp.issueflow.common.exception.BadRequestException;
import com.att.tdp.issueflow.common.exception.NotFoundException;
import com.att.tdp.issueflow.project.entity.ProjectMember;
import com.att.tdp.issueflow.project.entity.ProjectMemberId;
import com.att.tdp.issueflow.project.repository.ProjectMemberRepository;
import com.att.tdp.issueflow.project.entity.Project;
import com.att.tdp.issueflow.project.repository.ProjectRepository;
import com.att.tdp.issueflow.ticket.entity.Ticket;
import com.att.tdp.issueflow.ticket.enums.TicketPriority;
import com.att.tdp.issueflow.ticket.enums.TicketStatus;
import com.att.tdp.issueflow.ticket.enums.TicketType;
import com.att.tdp.issueflow.ticket.repository.TicketRepository;
import com.att.tdp.issueflow.user.entity.User;
import com.att.tdp.issueflow.user.enums.Role;
import com.att.tdp.issueflow.user.repository.UserRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TicketCsvService {

	private static final String ENTITY_TYPE = "TICKET";

	private static final CSVFormat EXPORT_FORMAT = CSVFormat.DEFAULT.builder()
			.setHeader("id", "title", "description", "status", "priority", "type", "assigneeId")
			.build();

	private static final CSVFormat IMPORT_FORMAT = CSVFormat.DEFAULT.builder()
			.setHeader()
			.setSkipHeaderRecord(true)
			.setTrim(true)
			.build();

	private final TicketRepository ticketRepository;
	private final ProjectRepository projectRepository;
	private final ProjectMemberRepository projectMemberRepository;
	private final UserRepository userRepository;
	private final AuditService auditService;

	public TicketCsvService(
			TicketRepository ticketRepository,
			ProjectRepository projectRepository,
			ProjectMemberRepository projectMemberRepository,
			UserRepository userRepository,
			AuditService auditService
	) {
		this.ticketRepository = ticketRepository;
		this.projectRepository = projectRepository;
		this.projectMemberRepository = projectMemberRepository;
		this.userRepository = userRepository;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public byte[] exportTickets(Long projectId) {
		if (!projectRepository.existsByIdAndDeletedAtIsNull(projectId)) {
			throw new NotFoundException("Project not found: " + projectId);
		}
		List<Ticket> tickets = ticketRepository.findAllByProject_IdAndDeletedAtIsNull(projectId);
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			 OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
			 CSVPrinter printer = new CSVPrinter(writer, EXPORT_FORMAT)) {
			for (Ticket ticket : tickets) {
				printer.printRecord(
						ticket.getId(),
						ticket.getTitle(),
						ticket.getDescription(),
						ticket.getStatus(),
						ticket.getPriority(),
						ticket.getType(),
						ticket.getAssignee() == null ? "" : ticket.getAssignee().getId()
				);
			}
			printer.flush();
			return outputStream.toByteArray();
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to export tickets", exception);
		}
	}

	@Transactional
	public TicketImportResultResponse importTickets(Long projectId, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("CSV file is required");
		}
		Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
				.orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

		int created = 0;
		int failed = 0;
		List<String> errors = new ArrayList<>();
		try (CSVParser parser = CSVParser.parse(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8),
				IMPORT_FORMAT)) {
			for (CSVRecord record : parser) {
				long rowNumber = record.getRecordNumber() + 1;
				try {
					Ticket ticket = buildTicketFromRecord(project, record);
					Ticket saved = ticketRepository.save(ticket);
					auditService.recordUserAction(
							AuditAction.CREATE,
							ENTITY_TYPE,
							saved.getId(),
							"{\"source\":\"tickets-csv-import\",\"projectId\":%d,\"row\":%d}".formatted(
									projectId,
									rowNumber
							)
					);
					created++;
				} catch (RuntimeException exception) {
					failed++;
					errors.add("row " + rowNumber + ": " + exception.getMessage());
				}
			}
		} catch (IOException exception) {
			throw new BadRequestException("Invalid CSV file");
		}

		return new TicketImportResultResponse(created, failed, errors);
	}

	private Ticket buildTicketFromRecord(Project project, CSVRecord record) {
		String title = requiredField(record, "title");
		String statusRaw = requiredField(record, "status");
		String priorityRaw = requiredField(record, "priority");
		String typeRaw = requiredField(record, "type");

		Ticket ticket = new Ticket();
		ticket.setProject(project);
		ticket.setTitle(title);
		ticket.setDescription(optionalField(record, "description"));
		ticket.setStatus(parseEnum(TicketStatus.class, statusRaw, "status"));
		ticket.setPriority(parseEnum(TicketPriority.class, priorityRaw, "priority"));
		ticket.setType(parseEnum(TicketType.class, typeRaw, "type"));
		ticket.setOverdue(false);

		String assigneeRaw = optionalField(record, "assigneeId");
		if (assigneeRaw != null && !assigneeRaw.isBlank()) {
			Long assigneeId;
			try {
				assigneeId = Long.parseLong(assigneeRaw);
			} catch (NumberFormatException exception) {
				throw new BadRequestException("invalid assigneeId");
			}
			User assignee = userRepository.findById(assigneeId)
					.orElseThrow(() -> new NotFoundException("Assignee user not found: " + assigneeId));
			if (assignee.getRole() != Role.DEVELOPER) {
				throw new BadRequestException("Assignee must have role DEVELOPER");
			}
			ensureProjectDeveloperMembership(project, assignee);
			ticket.setAssignee(assignee);
		}
		return ticket;
	}

	private void ensureProjectDeveloperMembership(Project project, User user) {
		if (projectMemberRepository.existsByProject_IdAndUser_Id(project.getId(), user.getId())) {
			return;
		}
		ProjectMember member = new ProjectMember();
		member.setId(new ProjectMemberId(project.getId(), user.getId()));
		member.setProject(project);
		member.setUser(user);
		member.setCreatedAt(java.time.Instant.now());
		projectMemberRepository.save(member);
	}

	private String requiredField(CSVRecord record, String fieldName) {
		String value = optionalField(record, fieldName);
		if (value == null || value.isBlank()) {
			throw new BadRequestException(fieldName + " is required");
		}
		return value;
	}

	private String optionalField(CSVRecord record, String fieldName) {
		if (!record.isMapped(fieldName)) {
			return null;
		}
		return record.get(fieldName);
	}

	private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String fieldName) {
		try {
			return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
		} catch (Exception exception) {
			throw new BadRequestException("invalid " + fieldName);
		}
	}
}
