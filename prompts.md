# AI Usage Log (`prompts.md`)

## Model

- Primary AI tool used: Cursor agent mode (`Codex 5.3`)
- Additional AI assistance was used for explanation, review, planning refinement, and documentation wording.

## Purpose of This File

This file documents how AI tools were used during the development of the IssueFlow home assignment.

The AI was used as a development assistant for:

- Understanding the assignment requirements.
- Planning the implementation phases.
- Reviewing architecture and package structure.
- Clarifying Spring Boot, JPA, JWT, Swagger/OpenAPI, and Flyway concepts.
- Designing tests and manual Swagger verification flows.
- Improving documentation quality.

All generated or AI-assisted suggestions were manually reviewed before being accepted.  
Final responsibility for the submitted code, architecture, documentation, and tests remains mine.

---

## AI-Assisted Workflow Summary

### 1. Planning and Phase Breakdown

AI was used to analyze the assignment PDF, the provided `README.md`, the project skeleton, and existing configuration files.

Main goals:

- Understand the system requirements.
- Identify the expected API contract.
- Propose a suitable Spring Boot architecture.
- Break the implementation into logical phases.
- Avoid over-engineering while still producing a professional backend solution.

Main artifacts:

- `plan/detailedWorkPlan.md`
- `plan/phase0_plan.md`
- `plan/phase1_Plan.md`
- `plan/phase2_Plan.md`
- `plan/phase3_Plan.md`
- `plan/phase4_Plan.md`
- `plan/phase5_Plan.md`

### 2. Architecture and Package Structure

AI was used to compare package-structure options and decide whether to organize the project by layers or by business features.

Final decision:

- Use a feature-based package structure.
- Keep each business area grouped together, such as `user`, `project`, `ticket`, `comment`, `audit`, and `security`.
- Keep JPA entities under `entity`.
- Keep enums under dedicated `enums` packages.
- Keep DTOs under `dto`.
- Keep shared exceptions and base persistence classes under `common`.

This structure was selected because it keeps each business domain easier to understand and maintain as the project grows.

### 3. Contract Alignment and Business Rules

AI was used to review and reason about business rules from the assignment and README contract.

Areas reviewed:

- Authentication and JWT flow.
- RBAC infrastructure.
- Soft delete and restore flows.
- Optimistic locking.
- Ticket status transitions.
- DONE ticket immutability.
- Ticket dependencies.
- Comments and mentions.
- Attachments.
- CSV import/export.
- Auto-assignment.
- Scheduler-based ticket escalation.
- Audit logging.

The implementation was kept aligned with the required API contract and the assignment constraints.

### 4. Security and Authentication

AI was used to help reason about Spring Security concepts and the JWT-based authentication flow.

Topics reviewed:

- `UserDetails`
- `UserDetailsService`
- `AuthenticationManager`
- `AuthenticationProvider`
- `SecurityFilterChain`
- JWT signing with HS256
- Token expiration
- Logout using server-side deny-list
- Swagger JWT authorization setup

The final implementation keeps authentication stateless and uses JWT Bearer tokens for protected endpoints.

### 5. Testing and Verification

AI was used to prepare manual and automated testing guidance.

Main testing artifacts:

- `plan/swagger-tests-phases0-3.txt`
- `plan/swagger-tests-phase0-4.txt`
- `plan/swagger-tests-phase5-scheduler-automation.txt`

The Swagger test files document:

- Setup steps.
- Login flow.
- JWT authorization.
- Request JSON examples.
- Expected status codes.
- Negative cases.
- Database verification queries.
- Scheduler verification steps.

<!-- ### 6. Final Documentation and Hardening

AI was used to improve final documentation and prepare submission-ready files.

Main artifacts:

- `run.md`
- `prompts.md`

The final documentation explains:

- How to start the database.
- How to run the application.
- How to use Swagger.
- How to login with the seeded admin user.
- How to run tests.
- How to reset the local database.
- How AI was used during the project.

--- -->

# Representative Prompts

The following prompts are representative examples of how AI was used during the project.  
They were rewritten for clarity, grammar, and readability while preserving the original intent.

---

## Prompt 1 — Initial Assignment Analysis and Work Plan

```text
I am applying for the TDP program at AT&T, and I am currently working on the home assignment stage.

I chose to implement the project using Java and Spring Boot.

The project folder includes:
- The project skeleton I received
- README.md
- A PDF file with the assignment instructions
- Existing source code and configuration files

Please carefully read all relevant files in the project:
- The assignment instructions PDF
- README.md
- pom.xml
- compose.yml
- The src folder structure
- Any existing file that may affect the implementation

Important:
Do not skip any part.
Do not make assumptions before checking the actual files.
Do not miss any requirement from the assignment.
If there is any contradiction between the PDF instructions and README.md, mention it explicitly and explain which source should be followed.

After reading the files, prepare a detailed and clear work plan for starting the project.

The work plan should include:
1. A short summary of what you understood from the assignment and the existing project.
2. A list of the main requirements identified from the files.
3. A recommended architecture for this Spring Boot project, including why it fits the assignment.
4. A recommended folder/package structure.
5. A breakdown into implementation phases in a logical order.
6. For each phase: what needs to be done, which areas of the project will be affected, and the expected outcome.
7. Important points to pay attention to, especially business rules, validation, authorization, error handling, database persistence, and documentation.
8. A recommendation for integrating Swagger/OpenAPI so endpoints can be tested without a frontend.
9. A recommendation for documenting the work process and AI usage in a prompts.md file.
10. A recommendation for which tests should be written and at which stage.

The solution should be:
- Understandable
- Readable
- Well-organized
- Suitable for a home assignment
- Not overly complicated

Please also be critical of the existing project. If dependencies, files, configurations, structure, or anything else is missing or unclear, mention it in the plan.
```

Outcome:

- A phase-based implementation plan was created.
- The project architecture was planned before writing feature code.
- Swagger, testing, and AI usage documentation were included in the plan.

---

## Prompt 2 — Phase 0 Planning

```text
I want to implement the project using the recommended package structure from the detailed work plan.

Now I want to implement Phase 0.

Please write a detailed plan for this phase.

Read the current project files carefully and follow the assignment instructions.
Do not skip requirements.
Keep the implementation simple, clean, and suitable for a home assignment.
```

Outcome:

- A foundation phase was planned.
- Basic project structure, common persistence classes, exceptions, enums, and infrastructure setup were defined.

---

## Prompt 3 — Phase 1 Planning

```text
I want to implement Phase 1 of the project.

Please write a detailed plan for this phase based on:
- The detailed work plan
- The assignment requirements
- The current project code

Read the current code carefully.
Follow the instructions exactly.
Do not add unnecessary features.
Keep the solution readable and explainable.
```

Outcome:

- Core entities and initial CRUD flows were planned.
- The implementation order was kept controlled and incremental.

---

## Prompt 4 — Phase 2 Planning: Authentication and Security

```text
I want to implement Phase 2.

Please write a detailed plan for this phase.

Focus on the authentication and security requirements from the assignment.

The plan should cover:
- Login
- Logout
- JWT authentication
- Current user endpoint
- Basic role infrastructure
- Swagger authorization with JWT
- Security configuration
- Tests needed for this phase

Please read the current code carefully.
Follow the assignment instructions.
Do not add unnecessary features.
```

Outcome:

- JWT-based authentication was planned.
- The project added a clean separation between `auth`, `jwt`, and `security config`.
- Swagger JWT authorization support was included.

---

## Prompt 5 — Package Structure Review

```text
I want to review the package structure.

I understand the feature-based structure, but I want to make sure it is the right choice.

Why should each business object have its own package with entity, repository, service, dto, and enums?
Why not create global folders such as entities, repositories, and services?

Also, enums are not entities, so I do not want them placed under an entity package.

Please analyze the best package structure for this assignment and recommend a clean approach.
```

Outcome:

- The project kept a feature-based package structure.
- Enums were separated into dedicated `enums` packages.
- The structure became easier to explain and more semantically correct.

---

## Prompt 6 — Moving Enums to Dedicated Packages

```text
I want to use dedicated enum packages, for example:
- user/enums
- ticket/enums
- audit/enums

Please update the necessary files and imports accordingly.
Keep entities only in entity packages.
```

Outcome:

- Enums were separated from JPA entities.
- Imports were updated accordingly.
- Package naming became cleaner.

---

## Prompt 7 — Phase 3 Planning

```text
I want to implement Phase 3.

Please write a detailed plan for this phase based on:
- The detailed work plan
- The assignment requirements
- The current implementation

Read the current code carefully.
Do not change README.md.
Follow the instructions.
Do not add unnecessary things.
```

Outcome:

- Additional ticket-related behavior and business rules were planned.
- The phase remained aligned with the assignment scope.

---

## Prompt 8 — User Versioning Design Question

```text
Why does the User entity need to inherit from VersionedAuditableEntity instead of only AuditableEntity?

Please explain whether optimistic locking is required for User, and whether this design choice is appropriate for this assignment.
```

Outcome:

- The difference between auditable entities and versioned auditable entities was reviewed.
- The design tradeoff was documented.
- Versioning was treated as a consistency/safety choice rather than a strict assignment requirement for every entity.

---

## Prompt 9 — Phase 4 Planning

```text
I want to implement Phase 4.

Please write a detailed plan for this phase.

Read the current code carefully.
Do not change README.md.
Follow the assignment instructions.
Do not add unnecessary features.
Keep the implementation understandable and suitable for a home assignment.
```

Outcome:

- Extended features were planned in a controlled way.
- Implementation remained incremental and aligned with the README/API contract.

---

## Prompt 10 — Default Password Design Review

```text
In the current code, each user is created with a default password.
However, during login, the user must provide a password even though no password was entered during user creation.

Is this a good design?
Please analyze the issue.
```

Outcome:

- The default-password flow was identified as weak if hardcoded.
- The design was improved by supporting an optional password during user creation and using a configurable fallback initial password for compatibility.

---

## Prompt 11 — Recommended Password Handling Approach

```text
Please recommend what to do about the user creation and login password flow.

In my opinion, it does not look good that login requires a password, but user creation does not clearly define one.

Analyze the best approach for this assignment.
The solution should remain compatible with the assignment API contract and should not be over-engineered.
```

Outcome:

- A hybrid solution was selected:
  - Keep the existing API contract compatible.
  - Allow an optional password during user creation.
  - Use a configurable initial password only when no password is provided.
  - Document this behavior in `run.md`.

---

## Prompt 12 — Phase 5 Planning: Scheduler and Automation

```text
I want to implement Phase 5.

Please write a detailed plan for this phase.

Read the current code carefully.
Do not change README.md.
Follow the assignment instructions.
Do not add unnecessary features.

Focus on scheduler and system automation behavior, including:
- Automatic overdue ticket detection
- Priority escalation
- isOverdue updates
- Audit logging for system actions
- Configuration needed for local testing
- Tests and verification steps
```

Outcome:

- Scheduler-based escalation was planned.
- Tests were documented for overdue tickets, priority changes, and database verification.

---

# AI-Assisted Changes: High-Level Summary

AI assistance contributed to:

- Creating a phase-based development plan.
- Reviewing and refining the package architecture.
- Explaining Spring Boot and JPA implementation details.
- Designing the authentication and JWT flow.
- Reasoning about logout and token revocation.
- Reviewing password-handling decisions.
- Preparing Swagger manual test documentation.
- Preparing scheduler verification documentation.

---

# Manual Verification and Accountability

I manually reviewed all AI-assisted changes before accepting them.

I validated the implementation through:

- Code review.
- Local application runs.
- Swagger manual endpoint checks.
- Database verification queries.
- Test execution.
- Review of alignment with README and assignment requirements.

The AI was used as an assistant for planning, review, explanation, and documentation.  
It was not used as a replacement for understanding the system.

Final responsibility for all submitted code, documentation, and design decisions remains mine.
