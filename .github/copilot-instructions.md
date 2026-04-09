# Copilot Instructions

This repository is a Quarkus 3 REST API built with Maven and Java 21, modeled after the Swagger Petstore contract.

## Stack and Structure

- Use Java 21 language features only.
- Build and run with the Maven wrapper: `./mvnw`.
- Main application code lives under `src/main/java/org/acme`, organized by domain:
  - `org.acme.pet` — pet registration and queries
  - `org.acme.store` — inventory and order management
  - `org.acme.user` — user operations
  - `org.acme.shared` — cross-cutting components (logging filter, shared types)
- Each domain follows a consistent internal structure:
  - `resources/` — JAX-RS resource classes
  - `resources/dtos/` — request and response record types
  - `services/` — application service classes
  - `services/mappers/` — entity-to-DTO mapper classes
  - `persistence/` — JPA entity and repository classes
- Static web assets live under `src/main/resources/META-INF/resources`.
- Do not edit generated files under `target/`.

## Coding Guidelines

- Introduce new code inside the appropriate domain package (`org.acme.pet`, `org.acme.store`, `org.acme.user`). Use `org.acme.shared` only for cross-cutting concerns.
- Follow the existing style: small resource classes, service layer for business logic, repository layer for persistence.
- Prefer focused changes over broad refactors.
- Preserve the existing API behavior unless the task explicitly requires a contract change.
- When adding or changing endpoints, keep request and response payloads simple and compatible with Jackson serialization.
- Use Java records for DTOs (request/response types).
- Use Lombok `@RequiredArgsConstructor` for constructor injection in resource and service classes.
- All input DTOs must use Bean Validation annotations (`@NotBlank`, `@NotNull`, `@Valid`, etc.).
- HTTP error responses follow RFC 7807 (`application/problem+json`), handled automatically by the `quarkus-resteasy-problem` extension when throwing JAX-RS exceptions like `NotFoundException`.
- Code formatting is enforced by Spotless (Google Java Format). Run `./mvnw spotless:apply` before committing.

## Testing Expectations

- Add or update tests for behavior changes.
- Use `@QuarkusTest` for application tests and RestAssured for HTTP assertions, matching the existing test style.
- Domain test classes live in domain-specific packages:
  - `src/test/java/org/acme/pet/resources/`
  - `src/test/java/org/acme/store/resources/`
  - `src/test/java/org/acme/user/resources/`
- Cross-cutting tests (OpenAPI, Swagger UI) live under `src/test/java/org/acme/rest/json/`.
- Integration tests that run against the packaged binary use `@QuarkusIntegrationTest` and follow the naming convention `*IT.java`.
- If native-specific behavior is added, keep the existing integration test pattern in mind.

## Useful Commands

- Start dev mode: `./run.sh` (or `./mvnw quarkus:dev`)
- Run tests and formatting check: `./run-check.sh` (or `./mvnw test spotless:check`)
- Auto-fix formatting: `./run-spotless-apply.sh` (or `./mvnw spotless:apply`)
- Full production build: `./run-build-prod.sh` (or `./mvnw clean verify`)
- Build and run Docker image: `./run-docker.sh`
- Windows equivalents: `run.cmd`, `run-check.cmd`, `run-spotless-apply.cmd`, `run-build-prod.cmd`, `run-docker.cmd`
- Makefile shortcuts: `make dev`, `make check`, `make fmt`, `make build`, `make docker`

## Change Hygiene

- Update related tests in the same change when modifying resources or payload models.
- Avoid introducing new dependencies unless they are necessary for the task.
- Keep README and runnable commands accurate if build or run behavior changes.
- Verify formatting passes before submitting: `./mvnw spotless:check`.
