# IssueFlow Project Documentation

## 1) Project Overview

IssueFlow is a backend REST API for lightweight project and ticket management.  
The system is designed to support day-to-day team coordination: creating projects, tracking tickets, assigning work, discussing implementation details, and auditing key actions.

The main domain objects are:

- users
- projects
- tickets
- comments
- audit logs
- ticket dependencies
- attachments
- project members

## 2) Main Capabilities

The implemented backend includes:

- JWT authentication (`/auth/login`, `/auth/logout`, `/auth/me`)
- User management
- Project management
- Ticket management
- Comment management
- Audit log retrieval
- Ticket dependency management
- Attachment upload/delete flows
- CSV import/export for tickets
- Soft delete and restore
- Mentions support in comments
- Project workload endpoint
- Auto-assignment on ticket creation
- Auto-escalation scheduler for overdue tickets

## 3) System Architecture

IssueFlow follows a layered Spring Boot architecture with clear separation between API, business logic, and persistence concerns.

Architecture diagram:

<img src="architecture%20diagram.png" alt="IssueFlow Architecture" width="500" />

## 4) Architecture Explanation

The architecture can be read from top to bottom:

- **Client / Swagger / Postman**: API consumers call HTTP endpoints. Swagger is used for discovery and manual verification.
- **Security Layer**: Spring Security + JWT validate authentication and enforce authorization rules (including role-based checks).
- **REST Controllers**: Controllers expose API contracts and delegate to services.
- **Business Services**: Services implement business rules such as state transitions, validation, assignment logic, and audit events.
- **System Automation**: Scheduled automation handles overdue escalation and system-generated changes.
- **Supporting Modules**: Cross-cutting modules (common exceptions, mapping helpers, validation utilities, audit helpers) keep the codebase consistent.
- **Persistence Layer**: Spring Data JPA repositories handle data access and query methods.
- **Database + Flyway**: PostgreSQL stores application data and Flyway tracks schema evolution through versioned migrations.

## 5) Technology Stack

- Java
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Maven Wrapper
- Swagger / OpenAPI
- JUnit / Spring Boot Test

## 6) Database and Persistence

IssueFlow uses PostgreSQL as the system database.  
Schema creation and evolution are managed through Flyway migrations under `src/main/resources/db/migration`.

At a high level, the persistence model includes tables/entities for:

- users
- projects
- tickets
- comments
- comment mentions
- ticket dependencies
- attachments
- audit logs
- revoked tokens
- project members

`project_members` exists to model which developers are linked to each project.  
This relationship is required for two implemented behaviors:

- workload calculation per project member
- auto-assignment of new tickets to the least-loaded linked DEVELOPER

## 7) Database Relationships

The database relationship diagram is documented in [`database-relationships.md`](database-relationships.md).

That document contains:

- a Mermaid ERD of the current schema
- brief relationship explanations for the main table connections

The full relationships document is intentionally kept separate and is not duplicated here.

## 8) Security

IssueFlow uses JWT-based authentication with Spring Security.

- Users authenticate through login and receive an access token.
- Most business endpoints require a Bearer token in the `Authorization` header.
- Logout is implemented through token revocation (deny-list behavior).
- ADMIN-only behavior is enforced where relevant, including endpoints for listing/restoring deleted records.

## 9) Automation

Two key automation behaviors are implemented:

- **Auto-assignment**: when a ticket is created without `assigneeId`, the system selects the least-loaded linked DEVELOPER from the same project.
- **Auto-escalation**: a scheduler scans overdue, non-DONE tickets and escalates priority step-by-step up to `CRITICAL`, while updating `isOverdue` according to the implemented escalation rules.

## 10) Testing

The test suite covers the major functional and integration flows, including:

- authentication and token behavior
- core CRUD flows
- persistence contracts
- soft delete and restore
- dependencies
- mentions
- attachments
- CSV import/export
- audit logs
- workload and auto-assignment
- escalation scheduler behavior

Run tests with:

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

## 11) Running the Project

For full setup/build/run instructions, use `run.md`.  
That file contains prerequisites, Docker database startup, build commands, run commands, and reset/stop guidance.

Swagger can be used for manual API testing once the service is running.

## 12) Swagger / Manual API Testing

Swagger UI:

- `http://localhost:8080/swagger-ui/index.html`

After logging in, copy the returned JWT and set authorization in Swagger as:

- `Bearer <accessToken>`

## 13) AI Usage

AI usage is documented in `prompts.md`.  
AI was used as a development assistant for planning, review, and documentation support, and the final code/documentation were reviewed and understood before submission.

## 14) Project Structure

Main package areas:

- `security`: authentication, JWT, and security configuration
- `user`: user APIs, services, entities, and role handling
- `project`: project APIs, project membership, and workload-related logic
- `ticket`: ticket APIs, service rules, CSV handling, dependencies, and scheduler integration
- `comment`: comments and mention mapping
- `audit`: audit logging and retrieval
- `attachment`: attachment management APIs and persistence
- `common`: shared exceptions, API error model, and base persistence/utilities

## 15) Final Notes

This document describes the implemented IssueFlow project as submitted.  
Detailed environment setup and exact run commands are in `run.md`.
