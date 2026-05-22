---
name: IssueFlow Phase 0 Plan
overview: Detailed Phase 0 foundation plan to align the current Spring Boot skeleton with the recommended package structure and establish a clean baseline for later phases.
todos:
  - id: phase0-dependencies
    content: Update Maven dependencies for Security, JWT, OpenAPI, and Flyway while preserving current test/runtime baseline.
    status: completed
  - id: phase0-config-profiles
    content: Refactor local/test configuration to a clean profile model and remove SQL-init/DDL drift risks.
    status: completed
  - id: phase0-migrations
    content: Initialize Flyway baseline migration and retire placeholder schema.sql/data.sql behavior.
    status: completed
  - id: phase0-package-skeleton
    content: Create Step 5 package skeleton under com.att.tdp.issueflow with module placeholders.
    status: completed
  - id: phase0-foundation-shells
    content: Add minimal common/security scaffolding classes without implementing domain/auth logic.
    status: completed
  - id: phase0-verification
    content: Run startup/tests and confirm done criteria for Phase 0 baseline.
    status: completed
isProject: false
---

# Phase 0 Detailed Implementation Plan

## Goal

Establish a clean, reproducible foundation for the project so Phase 1+ can be implemented in the Step 5 recommended package structure without rework.

## Scope Boundaries (Phase 0 only)

- Include: dependency baseline, configuration/profile hygiene, migration strategy, package skeleton bootstrap, and startup verification.
- Exclude: domain entities, business logic, controllers/services/repositories for User/Project/Ticket/Comment (these start in Phase 1).

## Target Structure Alignment (from Step 5)

Create package skeletons under `com.att.tdp.issueflow` now, so later phases can drop implementation directly into place:

- `common.exception`, `common.api`, `common.validation`, `common.mapper`
- `security.config`, `security.jwt`, `security.auth`
- feature roots prepared (empty for now): `user`, `project`, `ticket`, `comment`, `attachment`, `audit`
- ticket sub-roots prepared: `ticket.scheduler`, `ticket.csv`, `ticket.dependency`, `ticket.workload`

## Work Plan

### 1) Baseline Dependency Hardening (`pom.xml`)

Update [c:/Users/vered/CursorProjects/issueflow-java/pom.xml](c:/Users/vered/CursorProjects/issueflow-java/pom.xml) to support upcoming architecture:

- Add Spring Security starter.
- Add JWT library stack (JJWT api + impl + jackson runtime artifacts).
- Add OpenAPI UI dependency for contract visibility.
- Add Flyway core and database-postgresql integration.
- Keep `commons-csv` (already present) for future ticket CSV feature.
- Ensure test stack supports profile-driven tests (Spring Boot test + H2 remain).

Acceptance checks:

- `./mvnw -q -DskipTests dependency:tree` resolves cleanly.
- Application still starts with no missing bean/dependency failures.

### 2) Replace Placeholder SQL with Flyway-First Strategy

Current files are placeholders (`task` table) and conflict with assignment domain:

- [c:/Users/vered/CursorProjects/issueflow-java/src/main/resources/schema.sql](c:/Users/vered/CursorProjects/issueflow-java/src/main/resources/schema.sql)
- [c:/Users/vered/CursorProjects/issueflow-java/src/main/resources/data.sql](c:/Users/vered/CursorProjects/issueflow-java/src/main/resources/data.sql)

Actions:

- Disable legacy `spring.sql.init` startup path.
- Create migration directory and first baseline migration at `src/main/resources/db/migration`.
- Add `V1__baseline.sql` as an intentional minimal baseline (no `task` table), prepared for Phase 1 schema evolution.
- Either remove obsolete SQL files or leave them unused with explicit config preventing accidental execution.

Acceptance checks:

- Startup logs show Flyway migration execution.
- No `task` table creation/seed actions occur.

### 3) Profile Hygiene (`local`, `test`) and Config Cleanup

Refactor configuration for predictable environments:

- Normalize [c:/Users/vered/CursorProjects/issueflow-java/src/main/resources/application.yaml](c:/Users/vered/CursorProjects/issueflow-java/src/main/resources/application.yaml) into shared defaults + active profile guidance.
- Add/adjust local profile config (PostgreSQL + developer-friendly logging).
- Fix [c:/Users/vered/CursorProjects/issueflow-java/src/test/resources/application.yaml](c:/Users/vered/CursorProjects/issueflow-java/src/test/resources/application.yaml):
  - remove incorrect `platform: mssql`
  - use H2 in PostgreSQL compatibility mode
  - prefer migration-based startup in tests as well
  - set deterministic test-safe values.

Recommended config direction:

- `spring.jpa.hibernate.ddl-auto=validate` (or `none`) once Flyway is active.
- Explicit `spring.flyway.enabled=true`.
- Keep multipart limits (needed by attachment requirement later).

Acceptance checks:

- `local` profile boots against Docker Postgres.
- tests boot on H2 with no profile leakage from local.

### 4) Bootstrap Recommended Package Skeleton (No Business Logic Yet)

Create empty package placeholders (e.g., package-info or minimal marker classes/interfaces) in `src/main/java/com/att/tdp/issueflow` for:

- `common/*`, `security/*`, and feature roots listed in Step 5.

Purpose:

- Enforce intended modular boundaries from day one.
- Reduce churn when implementing Phases 1–4.

Acceptance checks:

- Build compiles with package skeleton in place.
- Team can implement Phase 1 files directly under predefined modules.

### 5) Foundation Guardrails (Minimal but Useful)

Add very small cross-cutting groundwork that belongs to foundation:

- `common.api` standard API error envelope (shape only).
- `common.exception` global exception handler shell (basic fallback).
- `security.config` placeholder security configuration class (deny-all or explicit temporary permit policy with TODO markers).

Constraint:

- Do not implement full auth behavior yet (reserved for Phase 2).

Acceptance checks:

- App boots with these shells and no ambiguous bean wiring.

### 6) Verification Matrix (Phase 0 Done Criteria)

Run and verify:

- application startup under local profile.
- `./mvnw test` under test profile.
- Flyway baseline migration runs in both environments.
- No legacy SQL init artifacts used.
- No package-structure drift from Step 5.

## Deliverables from Phase 0

- Updated dependency and plugin baseline in [c:/Users/vered/CursorProjects/issueflow-java/pom.xml](c:/Users/vered/CursorProjects/issueflow-java/pom.xml)
- Cleaned and profile-safe configuration in:
  - [c:/Users/vered/CursorProjects/issueflow-java/src/main/resources/application.yaml](c:/Users/vered/CursorProjects/issueflow-java/src/main/resources/application.yaml)
  - [c:/Users/vered/CursorProjects/issueflow-java/src/test/resources/application.yaml](c:/Users/vered/CursorProjects/issueflow-java/src/test/resources/application.yaml)
- Migration path initialized in `src/main/resources/db/migration/V1__baseline.sql`
- Placeholder SQL strategy resolved (`schema.sql` / `data.sql` removed or inert)
- Step 5 package skeleton created under `src/main/java/com/att/tdp/issueflow`
- Basic cross-cutting shells in `common` and `security` packages

## Risks and Mitigations

- Risk: mixed schema management (Hibernate auto-update + Flyway) causes drift.
  - Mitigation: move to Flyway-owned schema evolution in Phase 0.
- Risk: profile mismatch (current test config has `mssql` platform setting).
  - Mitigation: explicit test profile cleanup and migration-based boot.
- Risk: over-implementing security too early slows core progress.
  - Mitigation: keep security scaffolding minimal until Phase 2.

## Sequence for Execution

1. `pom.xml` dependency updates.
2. Config/profile cleanup.
3. Migration baseline + SQL init deprecation.
4. Package skeleton creation.
5. Minimal common/security shells.
6. Build + test + startup verification.

## Definition of Done (Phase 0)

- Project starts cleanly on local Postgres.
- Tests run on H2 with correct profile behavior.
- Flyway baseline is authoritative.
- Placeholder `task` SQL is no longer part of runtime behavior.
- Recommended package structure exists and is ready for Phase 1 implementation.
