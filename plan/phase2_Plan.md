---
name: phase2-security-auth-plan
overview: Implement Phase 2 (Security & Auth) end-to-end using JWT, auth endpoints, and RBAC foundations, aligned with project requirements and existing Phase 1 persistence artifacts.
todos:
  - id: phase2-config
    content: Define JWT/security properties and profile-safe defaults
    status: completed
  - id: phase2-jwt-core
    content: Implement JWT generation, validation, claim extraction
    status: completed
  - id: phase2-auth-service
    content: Implement login credential verification with PasswordEncoder
    status: completed
  - id: phase2-security-chain
    content: Integrate JWT filter and secure endpoint rules
    status: completed
  - id: phase2-logout-revocation
    content: Implement logout revocation flow and revoked-token enforcement
    status: completed
  - id: phase2-auth-endpoints
    content: Implement /auth/login, /auth/logout, /auth/me endpoints
    status: completed
  - id: phase2-rbac-foundation
    content: Map roles to authorities and enable method-level RBAC foundation
    status: completed
  - id: phase2-tests
    content: Add unit/integration tests for auth lifecycle and protected access
    status: completed
isProject: false
---

# Phase 2 Implementation Plan (Security & Auth)

## Scope

Implement only Phase 2 items:

- JWT-based authentication and request authorization
- Auth APIs: `POST /auth/login`, `POST /auth/logout`, `GET /auth/me`
- Role wiring (`ADMIN`, `DEVELOPER`) for upcoming RBAC checks
- Tests for token flow and protected access

## Inputs And Dependencies

- Requirements source: [C:/Users/vered/CursorProjects/issueflow-java/plan/TDP_issueflow_requirements.pdf](C:/Users/vered/CursorProjects/issueflow-java/plan/TDP_issueflow_requirements.pdf)
- Phase roadmap: [C:/Users/vered/CursorProjects/issueflow-java/plan/detailedWorkPlan.md](C:/Users/vered/CursorProjects/issueflow-java/plan/detailedWorkPlan.md)
- API contract: [C:/Users/vered/CursorProjects/issueflow-java/README.md](C:/Users/vered/CursorProjects/issueflow-java/README.md)
- Phase 1 outputs used by Phase 2: [C:/Users/vered/CursorProjects/issueflow-java/plan/phase1_Plan.md](C:/Users/vered/CursorProjects/issueflow-java/plan/phase1_Plan.md)

## Implementation Steps

1. **Define auth configuration surface**
   - Add JWT properties (secret, expiration seconds, issuer if needed) under existing config structure.
   - Ensure local/test profiles have deterministic values for tests.

2. **Build JWT core**
   - Implement token generation with claims: user identity + role + unique `jti` + expiry.
   - Implement token parsing/validation (signature, expiry, malformed token handling).
   - Expose utility methods for extracting principal and role from token.

3. **Implement authentication service**
   - Load user by username using repository.
   - Verify raw password against stored `passwordHash` via Spring `PasswordEncoder`.
   - Return login payload exactly as README contract (`accessToken`, `tokenType`, `expiresIn`).

4. **Implement HTTP security filter chain**
   - Add JWT filter that reads Bearer token, validates it, checks revocation, and sets `SecurityContext`.
   - Keep stateless session policy.
   - Permit only public endpoints (`/auth/login` and existing documentation/health endpoints); require authentication for all business APIs.

5. **Implement logout with revocation deny-list**
   - On logout, record token revocation using existing `RevokedToken` persistence.
   - On every authenticated request, reject revoked tokens.
   - Add cleanup path (scheduled/manual service method) for expired revocation entries.

6. **Implement auth controller endpoints**
   - `POST /auth/login`: validate request and return token response.
   - `POST /auth/logout`: revoke current token and return success with no extra payload.
   - `GET /auth/me`: return current authenticated user DTO without sensitive fields.

7. **Wire role model for RBAC foundation**
   - Map stored `Role` enum values into Spring authorities.
   - Enable method-level role checks for next phases; keep current Phase 2 endpoint behavior unchanged except auth requirements.

8. **Add focused tests for Phase 2**
   - Unit tests: JWT create/parse/expiry, auth service password validation paths.
   - Integration tests: login success/failure, unauthorized access to protected endpoint, valid token access, logout then token rejection, `/auth/me` correctness.

## File Targets

Primary areas expected for Phase 2 implementation:

- Security config and filters under `src/main/java/com/att/tdp/issueflow/security/config`
- JWT components under `src/main/java/com/att/tdp/issueflow/security/jwt`
- Auth API/service/DTOs under `src/main/java/com/att/tdp/issueflow/security/auth`
- Tests under `src/test/java/com/att/tdp/issueflow/security`
- Relevant configuration in `src/main/resources/application*.yaml`

## Done Criteria

Phase 2 is complete when all are true:

- Login returns valid JWT payload per README contract.
- All non-public APIs are protected and require Bearer JWT.
- Logout revokes active token and revoked token is denied afterward.
- `/auth/me` returns authenticated user profile (no secret fields).
- Role data is present in security context for future RBAC rules.
- Phase 2 tests pass via project test command.
