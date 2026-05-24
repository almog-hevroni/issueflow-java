# IssueFlow Run Guide

## Prerequisites

- Java 21 (or Java 25)
- Docker Desktop (or Docker Engine with Compose support)
- No global Maven installation is required (project uses Maven Wrapper)

## 1) Start Database

From the repository root:

```bash
docker compose up -d db
```

Verify the database container is running:

```bash
docker compose ps
```

## 2) Configuration Notes

The application uses `src/main/resources/application.yaml` as the main configuration file.
The project supports a `local` profile for local development/testing, and the default profile is already set to `local`.
For a deterministic local run, you can explicitly start the application with the `local` profile (commands are provided below).

By default, you do not need to set any environment variables for local run.

Optional environment variables:

- `ISSUEFLOW_SECURITY_JWT_SECRET`
  - Overrides the JWT signing secret.
  - If not set, the default from `application.yaml` is used.
  - The default value is intended for local development/testing only.
  - For any real deployment, this must be overridden via environment variable.

- `ISSUEFLOW_SECURITY_INITIAL_PASSWORD`
  - Used only when creating users via `POST /users` without a `password` value.
  - If not set, the fallback default is `Password123!`.
  - Note: this does **not** change the seeded admin credentials from migration.

## 3) Build

From the repository root:

```bash
# Linux / macOS
./mvnw clean package
```

```powershell
# Windows PowerShell
.\mvnw.cmd clean package
```

## 4) Run Application

From the repository root:

```bash
# Linux / macOS
./mvnw spring-boot:run
```

```powershell
# Windows PowerShell
.\mvnw.cmd spring-boot:run
```

To run explicitly with the `local` profile:

```bash
# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

```powershell
# Windows PowerShell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Using the explicit `local` profile ensures the application runs with the local development/testing configuration.

Application default URL:

- `http://localhost:8080`

## 5) Run Tests

From the repository root:

```bash
# Linux / macOS
./mvnw test
```

```powershell
# Windows PowerShell
.\mvnw.cmd test
```

## 6) Swagger / OpenAPI

Once the application is running:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Initial bootstrap user:

- A default admin user is seeded automatically by Flyway migration.
- Username: `admin`
- Password: `Password123!`

Authentication flow in Swagger:

1. Call `POST /auth/login` with `admin` / `Password123!` (or your configured credentials).
2. Copy `accessToken` from response.
3. Click **Authorize** in Swagger UI and enter: `Bearer <accessToken>`.
4. Run protected endpoints (including creating additional users via `POST /users`).

## 7) Quick Verification Flow

After the app is up, use this quick smoke test:

1. Start the database.
2. Run the application.
3. Open Swagger UI: `http://localhost:8080/swagger-ui/index.html`
4. Call `POST /auth/login` with:
   - username: `admin`
   - password: `Password123!`
5. Copy `accessToken` from the response.
6. Click Swagger **Authorize**.
7. Enter: `Bearer <accessToken>`
8. Call `GET /auth/me`.
9. Call `GET /users`.
10. Optional: create a user or project to verify protected endpoints end-to-end.

## 8) Reset Local Database

Use this only for local development/testing when you want a clean database and to rerun Flyway migrations from scratch.

```bash
docker compose down -v
docker compose up -d
```

Warning: this deletes the local Docker database volume and all local test data.

## 9) Stop Services

Stop app process in terminal (`Ctrl+C`), then stop database:

```bash
docker compose down
```
