# Copilot Instructions

This repository is a Quarkus 3 REST API built with Maven and Java 21, modeled after the Swagger Petstore contract.

## Stack and Structure

- Use Java 21 language features only.
- Build and run with the Maven wrapper: `./mvnw`.
- Main application code lives under `src/main/java/org/acme`, organized by domain:
  - `org.acme.pet` — pet registration and queries
  - `org.acme.store` — inventory and order management
  - `org.acme.user` — user operations
  - `org.acme.shared` — cross-cutting types such as `ApiResponse` and `LoggingFilter`
- The current domain layout is:
  - `resources/` — JAX-RS resource classes
  - `resources/dtos/` — request and response DTO records
  - `services/` — application service classes
  - `services/mappers/` — MapStruct mapper classes
  - `persistence/` — JPA entities, embeddables, and repositories
- `pet`, `store`, and `user` follow the same overall layout, with DTOs under `resources/dtos` and mappers under `services/mappers`.
- Static web assets live under `src/main/resources/META-INF/resources`.
- Project documentation may also exist under `docs/`.
- Do not edit generated files under `target/`.

## Coding Guidelines

- Add code inside the appropriate domain package. Use `org.acme.shared` only for genuinely cross-cutting concerns.
- Follow the existing layering: resource -> service -> repository.
- Keep resource classes small and put business rules in services.
- Prefer focused changes over broad refactors.
- Preserve the current API contract unless the task explicitly requires a breaking change.
- Keep request and response payloads simple and Jackson-friendly.
- Prefer Java records for DTOs and immutable value types when the current design supports it.
- Use Lombok `@RequiredArgsConstructor` for constructor injection in resource and service classes.
- Input DTOs should use Bean Validation annotations such as `@NotBlank`, `@NotNull`, and `@Valid`.
- HTTP error responses follow RFC 7807 (`application/problem+json`) via `quarkus-resteasy-problem`.
- MapStruct is available and existing mappers live under `services/mappers`.
- Query examples in this project intentionally cover Panache, `@NamedQuery`, `@NamedNativeQuery`, and Criteria API. Prefer those patterns over ad hoc JPQL string construction with `createQuery(...)`.
- Code formatting is enforced by Spotless with Google Java Format.

## Testing Expectations

- Add or update tests for every behavior change.
- Use `@QuarkusTest` for HTTP-level application tests with RestAssured.
- Domain tests live under:
  - `src/test/java/org/acme/pet/resources/`
  - `src/test/java/org/acme/store/resources/`
  - `src/test/java/org/acme/user/resources/`
- Cross-cutting tests such as OpenAPI and Swagger UI checks live under `src/test/java/org/acme/rest/json/`.
- Integration tests that run against the packaged application use `@QuarkusIntegrationTest` and the `*IT.java` naming pattern.
- When touching validation or error handling, assert the RFC 7807 payload shape, not only the status code.

## Useful Commands

- Start dev mode: `./run.sh` or `./mvnw quarkus:dev`
- Run tests and formatting check: `./run-check.sh` or `./mvnw test spotless:check`
- Auto-fix formatting: `./run-spotless-apply.sh` or `./mvnw spotless:apply`
- Full production build: `./run-build-prod.sh` or `./mvnw clean verify`
- Build and run the Docker image: `./run-docker.sh`
- Build the app package for local containers: `./mvnw package -DskipTests`
- Start the local observability stack: `docker compose up`
- Windows equivalents: `run.cmd`, `run-check.cmd`, `run-spotless-apply.cmd`, `run-build-prod.cmd`, `run-docker.cmd`
- Make targets: `make help`, `make dev`, `make check`, `make fmt`, `make build`, `make docker`

## Change Hygiene

- Update related tests in the same change when modifying resources, DTOs, or validation rules.
- Avoid introducing new dependencies unless they are necessary for the task.
- Keep README and runnable commands accurate if build, Docker, or local stack behavior changes.
- Verify formatting before submitting: `./mvnw spotless:check`.
