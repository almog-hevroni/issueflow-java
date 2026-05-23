---
name: phase3-core-crud-plan
overview: Implement Phase 3 by adding Users/Projects/Tickets/Comments CRUD APIs, core validations, status-transition rules, and audit logging on top of the existing Phase 1+2 schema and auth foundation.
todos:
  - id: phase3-dto-controller-service
    content: Create DTOs/controllers/services for users, projects, tickets, comments endpoints per README contract
    status: completed
  - id: phase3-ticket-rules
    content: Enforce ticket lifecycle forward-only transitions and DONE immutability in ticket service
    status: completed
  - id: phase3-soft-delete
    content: Implement project/ticket soft-delete behavior in delete endpoints and active-only reads
    status: completed
  - id: phase3-audit
    content: Integrate audit logging for all core create/update/delete operations with USER actor context
    status: completed
  - id: phase3-error-handling
    content: Extend global/domain exception handling for not found, validation, conflicts, and optimistic lock errors
    status: completed
  - id: phase3-tests
    content: Add integration tests for Phase 3 endpoint behavior, rule enforcement, and audit persistence
    status: completed
isProject: false
---

# Phase 3 Implementation Plan (Core CRUD APIs)

## Goal

Implement Phase 3 end-to-end for `Users`, `Projects`, `Tickets`, and `Comments` according to the API contract in [C:/Users/vered/CursorProjects/issueflow-java/README.md](C:/Users/vered/CursorProjects/issueflow-java/README.md), using the already implemented auth/security foundation and existing persistence model.

## Current Baseline (Verified)

- Auth is already implemented (`/auth/login`, `/auth/logout`, `/auth/me`) and all non-public routes are protected by JWT in [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/security/config/SecurityConfig.java](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/security/config/SecurityConfig.java).
- Core entities and repositories already exist for users/projects/tickets/comments and include optimistic locking (`@Version`) via [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/common/persistence/VersionedAuditableEntity.java](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/common/persistence/VersionedAuditableEntity.java).
- DB schema/migration already includes all relevant tables and constraints in [C:/Users/vered/CursorProjects/issueflow-java/src/main/resources/db/migration/V1\_\_baseline.sql](C:/Users/vered/CursorProjects/issueflow-java/src/main/resources/db/migration/V1__baseline.sql).
- Global error envelope exists in [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/common/exception/GlobalExceptionHandler.java](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/common/exception/GlobalExceptionHandler.java), but Phase 3 needs domain-specific exception mapping and optimistic-lock handling.

## Phase 3 Scope

- Users APIs: `GET /users`, `GET /users/:userId`, `POST /users`, `POST /users/update/:userId`, `DELETE /users/:userId`.
- Projects APIs: `GET /projects`, `GET /projects/:projectId`, `POST /projects`, `PATCH /projects/:projectId`, `DELETE /projects/:projectId` (soft-delete behavior).
- Tickets APIs (core only): `GET /tickets?projectId=:projectId`, `GET /tickets/:ticketId`, `POST /tickets`, `PATCH /tickets/:ticketId`, `DELETE /tickets/:ticketId` (soft-delete behavior).
- Comments APIs: `GET /tickets/:ticketId/comments`, `POST /tickets/:ticketId/comments`, `PATCH /tickets/:ticketId/comments/:commentId`, `DELETE /tickets/:ticketId/comments/:commentId`.
- Audit logs for all state-changing core operations (create/update/delete for these resources).

## Implementation Steps

### 1) Add API DTOs and mapping layer for Phase 3 resources

Create request/response DTOs and mappers for users, projects, tickets, comments to keep entity boundaries clean.

Primary file targets:

- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/user](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/user)
- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/project](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/project)
- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket)
- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/comment](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/comment)

### 2) Implement Users service + controller

- Implement user CRUD with uniqueness checks (`username`, `email`) and role validation via enum.
- Keep update endpoint path exactly as contract: `POST /users/update/:userId`.
- Ensure safe delete behavior (reject delete when referenced by constraints, return clear API error).

Primary file targets:

- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/user/repository/UserRepository.java](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/user/repository/UserRepository.java)
- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/user](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/user)

### 3) Implement Projects service + controller

- Implement project CRUD with owner existence validation.
- For `DELETE /projects/:projectId`, set `deletedAt` (soft-delete) instead of hard delete.
- All standard reads (`GET /projects`, `GET /projects/:projectId`) return non-deleted projects only.

Primary file targets:

- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/project/repository/ProjectRepository.java](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/project/repository/ProjectRepository.java)
- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/project/entity/Project.java](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/project/entity/Project.java)
- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/project](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/project)

### 4) Implement Tickets service + controller with lifecycle rules

- Implement ticket CRUD for active (non-deleted) tickets.
- Enforce required rules in update flow:
  - No updates once status is `DONE`.
  - Status can only move forward: `TODO -> IN_PROGRESS -> IN_REVIEW -> DONE`.
- `DELETE /tickets/:ticketId` performs soft delete by setting `deletedAt`.
- Validate `projectId` and optional `assigneeId` existence before create/update.

Primary file targets:

- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/repository/TicketRepository.java](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/repository/TicketRepository.java)
- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/entity/Ticket.java](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/entity/Ticket.java)
- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket)

### 5) Implement Comments service + controller

- Implement list/create/update/delete for comments under ticket routes.
- Validate parent ticket existence and comment-ticket consistency (`commentId` must belong to path `ticketId`).
- Preserve optimistic locking behavior for concurrent comment edits.

Primary file targets:

- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/comment/repository/CommentRepository.java](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/comment/repository/CommentRepository.java)
- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/comment/entity/Comment.java](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/comment/entity/Comment.java)
- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/comment](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/comment)

### 6) Add audit logging integration for all state-changing core actions

Create an audit service and record at least:

- `CREATE`, `UPDATE`, `DELETE` for users/projects/tickets/comments.
- actor type `USER` with authenticated user ID.
- `entityType` and `entityId` aligned with each operation.

Primary file targets:

- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/audit/entity/AuditLog.java](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/audit/entity/AuditLog.java)
- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/audit/repository/AuditLogRepository.java](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/audit/repository/AuditLogRepository.java)
- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/audit](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/audit)

### 7) Harden domain error handling for Phase 3 behavior

Extend exception handling for predictable API errors:

- not found, conflict/duplicate, invalid state transition, immutable DONE ticket, optimistic lock conflict.
- map these to stable HTTP responses (`404`, `400`, `409`) with the existing error envelope.

Primary file targets:

- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/common/exception/GlobalExceptionHandler.java](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/common/exception/GlobalExceptionHandler.java)
- [C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/common/exception](C:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/common/exception)

### 8) Add Phase 3 integration tests (happy + validation/error paths)

Add focused MockMvc/SpringBoot tests for:

- each endpoint group (users/projects/tickets/comments), including key validation failures.
- ticket lifecycle forward-only and DONE immutability.
- optimistic locking conflict cases for tickets/comments.
- soft-delete visibility for projects/tickets in standard endpoints.
- audit log rows created by state-changing actions.

Primary file targets:

- [C:/Users/vered/CursorProjects/issueflow-java/src/test/java/com/att/tdp/issueflow](C:/Users/vered/CursorProjects/issueflow-java/src/test/java/com/att/tdp/issueflow)

## Definition of Done (Phase 3)

- All Phase 3 endpoints are implemented and authenticated.
- Users/projects/tickets/comments core CRUD works per README contract.
- Ticket lifecycle rules and DONE immutability are enforced in service layer.
- Soft-delete behavior is applied to projects/tickets delete endpoints and standard reads exclude deleted rows.
- Audit records are persisted for each core state-changing operation.
- Tests cover core happy paths and critical validation/business-rule failures.
- `README.md` remains unchanged.
