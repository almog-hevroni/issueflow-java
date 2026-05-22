---
name: Phase 1 Persistence Plan
overview: Detailed plan to implement Phase 1 (Domain Model + Persistence) for IssueFlow in Spring Boot, aligned to your existing repository state and assignment requirements.
todos:
  - id: phase1-entity-model
    content: Design and add all Phase 1 entities and enums with JPA mappings, including optimistic-lock fields and soft-delete columns.
    status: completed
  - id: phase1-repositories
    content: Implement Spring Data repositories with foundational query methods for non-deleted reads, project scoping, dependencies, mentions, and workload support.
    status: completed
  - id: phase1-migrations
    content: Create/adjust Flyway migration scripts to define full IssueFlow schema, foreign keys, constraints, and indexes in correct creation order.
    status: completed
  - id: phase1-persistence-tests
    content: Add repository/persistence tests covering schema bootstrap, key constraints, optimistic locking behavior, and soft-delete-ready query semantics.
    status: completed
  - id: phase1-verify-build
    content: Run build and test validation for Phase 1 persistence layer readiness before moving to Phase 2 security/auth.
    status: completed
isProject: false
---

# Phase 1 Detailed Implementation Plan

## Goal

Implement a complete persistence backbone for IssueFlow by introducing domain entities, JPA mappings, repositories, and Flyway schema migrations that support current and upcoming phases (security, CRUD APIs, audit, extended features) without rework.

## Scope Boundaries (Phase 1 Only)

- In scope:
  - JPA entities, enums, and relationships
  - Flyway SQL schema for all required tables
  - Spring Data repositories with foundational query methods
  - optimistic locking (`@Version`) at least for `Ticket` and `Comment` (optionally broadening to other mutable entities for consistency)
  - soft-delete columns and base filtering strategy preparation
- Out of scope:
  - controllers/services business logic
  - JWT/auth endpoints implementation
  - CSV, scheduler, attachment upload logic
  - API-level validation and exception mapping beyond persistence needs

## Existing Baseline to Reuse

- App entry point already exists in [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/IssueFlowApplication.java`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/IssueFlowApplication.java).
- Migration folder already exists with starter file [`c:/Users/vered/CursorProjects/issueflow-java/src/main/resources/db/migration/V1__baseline.sql`](c:/Users/vered/CursorProjects/issueflow-java/src/main/resources/db/migration/V1__baseline.sql).
- Package placeholders already exist for all major modules under [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow).

## Target Domain Model

### Core entities

- `User`
  - fields: `id`, `username`, `email`, `fullName`, `role`, `passwordHash`, `createdAt`, `updatedAt`, `version`
  - unique constraints: `username`, `email`
- `Project`
  - fields: `id`, `name`, `description`, `ownerId`, `deletedAt`, `createdAt`, `updatedAt`, `version`
- `Ticket`
  - fields: `id`, `projectId`, `title`, `description`, `status`, `priority`, `type`, `assigneeId`, `dueDate`, `isOverdue`, `deletedAt`, `createdAt`, `updatedAt`, `version`
- `Comment`
  - fields: `id`, `ticketId`, `authorId`, `content`, `createdAt`, `updatedAt`, `version`

### Extended-ready persistence entities (created now to avoid schema churn)

- `TicketDependency`
  - fields: `ticketId`, `blockedByTicketId`, `createdAt`
  - PK/unique composite to prevent duplicates
- `Attachment`
  - fields: `id`, `ticketId`, `uploadedById`, `fileName`, `contentType`, `sizeBytes`, `storagePath`, `createdAt`
- `CommentMention`
  - fields: `commentId`, `mentionedUserId`, `createdAt`
  - composite unique `(commentId, mentionedUserId)`
- `AuditLog`
  - fields: `id`, `actorType`, `actorUserId(nullable)`, `action`, `entityType`, `entityId`, `detailsJson`, `createdAt`
- `RevokedToken` (if deny-list logout path is chosen)
  - fields: `id`, `jti`, `tokenHash`, `expiresAt`, `revokedAt`
  - unique `jti` (or token hash fallback)

### Enums (Java + DB check constraints or varchar discipline)

- `Role`: `ADMIN`, `DEVELOPER`
- `TicketStatus`: `TODO`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`
- `TicketPriority`: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- `TicketType`: `BUG`, `FEATURE`, `TECHNICAL`
- `AuditActorType`: `USER`, `SYSTEM`
- `AuditAction`: include baseline actions (`CREATE`, `UPDATE`, `DELETE`, `RESTORE`, `AUTO_ASSIGN`, `AUTO_ESCALATE`, etc.)

## Relationships and Integrity Rules in Schema

- `project.owner_id -> users.id`
- `ticket.project_id -> projects.id`
- `ticket.assignee_id -> users.id (nullable)`
- `comment.ticket_id -> tickets.id`
- `comment.author_id -> users.id`
- `ticket_dependencies.ticket_id -> tickets.id`
- `ticket_dependencies.blocked_by_ticket_id -> tickets.id`
- `attachments.ticket_id -> tickets.id`
- `attachments.uploaded_by_id -> users.id`
- `comment_mentions.comment_id -> comments.id`
- `comment_mentions.mentioned_user_id -> users.id`

Additional DB constraints for correctness:

- prevent self-dependency (`ticket_id != blocked_by_ticket_id`)
- indexes on high-traffic filters (`project_id`, `ticket_id`, `assignee_id`, `deleted_at`, `created_at`)
- optional partial indexes for non-deleted rows where useful

## Concrete File Plan

### 1) Entity and enum package implementation

- Create package structure beneath each module, e.g.:
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/user/entity`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/user/entity)
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/project/entity`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/project/entity)
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/entity`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/ticket/entity)
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/comment/entity`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/comment/entity)
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/audit/entity`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/audit/entity)
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/attachment/entity`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/attachment/entity)
  - [`c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/security/auth/entity`](c:/Users/vered/CursorProjects/issueflow-java/src/main/java/com/att/tdp/issueflow/security/auth/entity)

### 2) Repository interfaces

- Add `JpaRepository` interfaces in each module repository package:
  - `UserRepository`, `ProjectRepository`, `TicketRepository`, `CommentRepository`, `TicketDependencyRepository`, `AttachmentRepository`, `CommentMentionRepository`, `AuditLogRepository`, `RevokedTokenRepository`.
- Add foundational methods needed by next phases:
  - soft-delete aware lookups (`findByIdAndDeletedAtIsNull`, `findAllByDeletedAtIsNull`)
  - project-scoped ticket queries
  - non-DONE workload counts per assignee
  - dependency blocker existence checks
  - mention retrieval by user ordered newest first

### 3) Flyway migration design

- Replace/expand baseline migration in [`c:/Users/vered/CursorProjects/issueflow-java/src/main/resources/db/migration/V1__baseline.sql`](c:/Users/vered/CursorProjects/issueflow-java/src/main/resources/db/migration/V1__baseline.sql) with full domain schema.
- If already shared with teammate history, keep `V1` minimal and add `V2__issueflow_domain.sql`; otherwise keep all in `V1`.
- Include:
  - table creation order respecting FK dependencies
  - explicit constraints + indexes
  - timestamp defaults
  - nullable strategy aligned with requirements

### 4) Persistence conventions (cross-cutting)

- Standardize column naming in snake_case (`created_at`, `deleted_at`, etc.).
- Use `@Enumerated(EnumType.STRING)` for all enums.
- Use `@Version` on at least `Ticket` and `Comment`.
- Introduce a small shared mapped superclass in `common` (optional) for `createdAt/updatedAt/version` if it reduces duplication and remains clear.

## Data Model Diagram

```mermaid
flowchart TD
  User -->|owns| Project
  Project -->|contains| Ticket
  User -->|assignedTo| Ticket
  Ticket -->|has| Comment
  User -->|authors| Comment
  Ticket -->|blockedBy| TicketDependency
  TicketDependency -->|references| Ticket
  Ticket -->|has| Attachment
  Comment -->|mentions| CommentMention
  CommentMention -->|targets| User
  AuditLog -->|actorUserId(optional)| User
  RevokedToken -->|authControl| User
```

## Verification Plan for Phase 1

- Startup validation:
  - app boots cleanly with Flyway migrations applied on an empty DB.
- Persistence sanity checks:
  - insert/read each entity via repository smoke tests.
- Constraint checks:
  - duplicate username/email rejected.
  - invalid enum persistence blocked at app boundary.
  - self-dependency blocked.
- Concurrency readiness:
  - optimistic lock conflict test for `Ticket` and `Comment` concurrent update simulation.
- Soft-delete readiness:
  - queries exclude soft-deleted rows when using non-deleted repository methods.

## Risks and Mitigations

- Over-design risk in Phase 1:
  - mitigate by keeping entities lean and deferring business logic to services in later phases.
- Migration churn risk:
  - mitigate by including extended-feature tables now so later phases mostly add behavior, not schema rewrites.
- FK cascade pitfalls:
  - mitigate by explicit `ON DELETE` strategy (prefer restrictive delete due to soft-delete model).

## Exit Criteria (Done Definition)

- All required tables exist via Flyway migration and match Phase 1 entities.
- Core repository layer compiles and supports key lookups needed by upcoming auth/CRUD phases.
- `Ticket` and `Comment` optimistic locking is demonstrably active.
- No placeholder `task` schema artifacts remain in active migration path.
- Project builds and Phase 1 persistence tests pass consistently.
