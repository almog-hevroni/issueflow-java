---
name: phase4-detailed-plan
overview: "Implement Phase 4 extended IssueFlow features on top of existing Phase 0–3 code: dependencies, attachments, CSV import/export, admin soft-delete restore/listing, mentions endpoint, workload, and auto-assignment. Keep API contract aligned with README and requirements PDF, without modifying README.md."
todos:
  - id: phase4-dependencies
    content: Implement dependency API and enforce unresolved blocker check before ticket DONE.
    status: completed
  - id: phase4-softdelete-admin
    content: Add admin-only deleted listing and restore endpoints for tickets/projects with RESTORE audit.
    status: completed
  - id: phase4-workload-autoassign
    content: Implement workload endpoint and auto-assignment during ticket creation with SYSTEM audit.
    status: completed
  - id: phase4-mentions-endpoint
    content: Add GET /users/{userId}/mentions endpoint using existing mention persistence.
    status: completed
  - id: phase4-attachments-csv
    content: Implement attachments upload/delete validation and ticket CSV export/import flows.
    status: completed
  - id: phase4-tests
    content: Add dedicated Phase 4 integration tests covering all new behaviors and edge cases.
    status: completed
isProject: false
---

# Phase 4 Detailed Implementation Plan

## Goal

Implement all Phase 4 features required by the contract while preserving existing Phase 0–3 behavior and tests. Do not modify [`c:/Users/vered/CursorProjects/issueflow-java/README.md`](c:/Users/vered/CursorProjects/issueflow-java/README.md).

## Current Baseline (from code)

- Core CRUD + auth are already implemented in controllers/services:
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/TicketController.java`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/TicketController.java)
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/TicketService.java`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/TicketService.java)
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/comment/CommentService.java`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/comment/CommentService.java)
- Phase 4 data model scaffolding exists:
  - dependencies table/entity/repository, attachments table/entity/repository, mention repository query, open-ticket count query.
- Missing pieces are mainly APIs + service logic + RBAC + tests.

## Scope for Phase 4

1. Ticket dependencies API + blocker validation before DONE.
2. Attachment upload/delete with MIME + size validation.
3. Ticket CSV export/import.
4. Soft-delete admin endpoints (list deleted + restore) for tickets/projects.
5. Mentions endpoint (`GET /users/{userId}/mentions`).
6. Project workload endpoint.
7. Auto-assignment on ticket create when `assigneeId` is missing.

## Implementation Steps

### 1) Dependencies API + DONE blocker rule

- Add dependency service/controller under `ticket.dependency`:
  - `POST /tickets/{ticketId}/dependencies`
  - `GET /tickets/{ticketId}/dependencies`
  - `DELETE /tickets/{ticketId}/dependencies/{blockerId}`
- Reuse:
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/dependency/repository/TicketDependencyRepository.java`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/dependency/repository/TicketDependencyRepository.java)
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/repository/TicketRepository.java`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/repository/TicketRepository.java)
- Add service validation:
  - both tickets exist and are active,
  - same project,
  - no duplicates,
  - prevent self-link (defensive check in service, DB already enforces).
- Extend `TicketService` transition logic so status change to `DONE` fails if any blocker is not `DONE`.

### 2) Attachments API + validation

- Create `AttachmentService` + `AttachmentController` under `attachment` module with:
  - `POST /tickets/{ticketId}/attachments`
  - `DELETE /tickets/{ticketId}/attachments/{attachmentId}`
- Validate:
  - max size 10 MB,
  - MIME allowlist: `image/png`, `image/jpeg`, `application/pdf`, `text/plain`.
- Store files under deterministic local path (e.g., ticket-scoped directory) and persist metadata via:
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/attachment/repository/AttachmentRepository.java`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/attachment/repository/AttachmentRepository.java)
- Ensure delete removes both DB row and file (best-effort with clear error behavior).

### 3) CSV export/import for tickets

- Implement dedicated CSV service in `ticket.csv` package (currently placeholder):
  - export endpoint: `GET /tickets/export?projectId=:projectId`
  - import endpoint: `POST /tickets/import` multipart (`file`, `projectId`)
- Use Apache Commons CSV (already in `pom.xml`) with robust quoting/parsing.
- Export fields exactly per contract: `id,title,description,status,priority,type,assigneeId`.
- Import behavior:
  - per-row validation,
  - continue processing valid rows,
  - return `{ created, failed, errors[] }` summary.

### 4) Admin soft-delete listing + restore

- Extend controllers/services:
  - tickets: `GET /tickets/deleted`, `POST /tickets/{id}/restore`
  - projects: `GET /projects/deleted`, `POST /projects/{id}/restore`
- Reuse repository methods already present for deleted rows.
- Add ADMIN-only authorization (method-level annotations) for these endpoints.
- Record audit with `RESTORE` action.

### 5) Mentions endpoint for user

- Add endpoint on user side:
  - `GET /users/{userId}/mentions`
- Reuse mention query:
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/comment/repository/CommentMentionRepository.java`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/comment/repository/CommentMentionRepository.java)
- Return newest-first comments including existing mention metadata DTO shape.
- Keep existing create/update mention sync logic unchanged in `CommentService`.

### 6) Workload endpoint

- Add endpoint:
  - `GET /projects/{projectId}/workload`
- Implement service returning `{ userId, username, openTicketCount }` sorted ascending.
- Use open-ticket count query already in `TicketRepository` and restrict candidate users to project members with `DEVELOPER` role (exclude ADMIN).

### 7) Auto-assignment on ticket create

- Update ticket creation flow in:
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/TicketService.java`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/TicketService.java)
- Rule:
  - if `assigneeId` provided, keep existing explicit assignment validation;
  - if missing, select least-loaded `DEVELOPER` in project;
  - tie-break by oldest registration order;
  - if no eligible developer, keep `assigneeId = null`.
- Record system audit action `AUTO_ASSIGN`.

## Cross-Cutting Adjustments

- Extend audit service API to support system actor records (for auto-assignment now; escalation later in Phase 5):
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/audit/AuditService.java`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/audit/AuditService.java)
- Keep exception style consistent through:
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/common/exception/GlobalExceptionHandler.java`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/common/exception/GlobalExceptionHandler.java)
- Add DTOs for new endpoints under each module (`ticket.dependency.dto`, `attachment.dto`, `ticket.csv.dto`, `project.dto` or `ticket.workload.dto`).

## Testing Plan (Phase 4 only)

- Add focused integration tests under `src/test/java/...` for:
  - dependency CRUD + DONE blocked by unresolved blockers,
  - attachment upload accept/reject (MIME and size) + delete,
  - CSV export/import with quotes/commas edge cases,
  - admin-only deleted/restore tickets/projects,
  - mentions endpoint ordering/content,
  - workload sorting and auto-assignment tie-break behavior.
- Keep existing `CoreCrudIntegrationTest` stable; add dedicated classes instead of inflating one large test.

## Execution Order

1. Dependencies (includes DONE blocker in ticket transition).
2. Admin deleted/restore endpoints + RBAC.
3. Workload service + auto-assignment in ticket create.
4. Mentions endpoint.
5. Attachments.
6. CSV import/export.
7. Phase 4 integration tests and Swagger checklist verification in [`c:/Users/vered/CursorProjects/issueflow-java/plan/swagger-tests-current.txt`](c:/Users/vered/CursorProjects/issueflow-java/plan/swagger-tests-current.txt).

## Non-Goals (explicit)

- No edits to README contract file.
- No Phase 5 scheduler/escalation implementation in this phase.
- No architectural refactor outside what is required for Phase 4 delivery.
