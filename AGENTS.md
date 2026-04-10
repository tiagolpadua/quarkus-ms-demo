# AGENTS.md

This file provides guidance for coding agents working in this repository.

## Project Summary

This repository is a Quarkus 3 REST API built with Maven and Java 21, modeled after the Swagger Petstore contract.

Core domains live under `src/main/java/org/acme`:

- `org.acme.pet` - pet registration and queries
- `org.acme.store` - inventory and order management
- `org.acme.user` - user operations
- `org.acme.shared` - cross-cutting concerns such as `ApiResponse` and `LoggingFilter`

Within each domain, keep the current layout:

- `resources/` - JAX-RS resource classes
- `resources/dtos/` - request and response DTO records
- `services/` - application services
- `services/mappers/` - MapStruct mappers
- `persistence/` - JPA entities and repositories

Static web assets live under `src/main/resources/META-INF/resources`.
Project documentation may also exist under `docs/`.
Do not edit generated files under `target/`.

## Commands

```bash
# Development mode
./run.sh
# or
./mvnw quarkus:dev

# Run tests and formatting checks
./run-check.sh
# or
./mvnw test spotless:check

# Auto-fix formatting
./run-spotless-apply.sh
# or
./mvnw spotless:apply

# Full production-style verification
./run-build-prod.sh
# or
./mvnw clean verify

# Build and run Docker image
./run-docker.sh

# Run one test class
./mvnw test -Dtest=PetResourceTest

# Start local observability stack
docker compose up

# Makefile shortcuts
make dev
make check
make fmt
make build
make docker
```

Windows equivalents are available through `run.cmd`, `run-check.cmd`, `run-spotless-apply.cmd`, `run-build-prod.cmd`, and `run-docker.cmd`.

## Architecture and Conventions

- Use Java 21 language features only.
- Build and run with the Maven wrapper: `./mvnw`.
- Follow the existing layering: resource -> service -> repository.
- Keep resource classes thin and place business rules in services.
- Add code in the appropriate domain package; use `org.acme.shared` only for genuinely cross-cutting concerns.
- Prefer focused changes over broad refactors.
- Preserve the current API contract unless the task explicitly requires a breaking change.
- Prefer Java records for DTOs and immutable value types where the current design supports them.
- Use Lombok `@RequiredArgsConstructor` for constructor injection in resource and service classes.
- Use Bean Validation on input DTOs with annotations such as `@NotBlank`, `@NotNull`, and `@Valid`.
- HTTP error responses follow RFC 7807 via `quarkus-resteasy-problem`.
- Use existing MapStruct mappers under `services/mappers`; do not expose entities directly in API responses.
- Prefer repository/query patterns already used in the project: Panache, `@NamedQuery`, `@NamedNativeQuery`, and Criteria API.
- Avoid ad hoc JPQL string construction with `createQuery(...)` when an established project pattern already fits.
- Formatting is enforced by Spotless with Google Java Format.

## Testing Expectations

- Add or update tests for every behavior change.
- Use `@QuarkusTest` for HTTP-level application tests with REST Assured.
- Use `@QuarkusIntegrationTest` and the `*IT.java` naming pattern for tests that run against the packaged application.
- When changing validation or error handling, assert the RFC 7807 response payload shape, not only the HTTP status.
- Keep domain tests close to the affected package structure under `src/test/java/org/acme/...`.
- Cross-cutting tests such as OpenAPI and Swagger UI checks live under `src/test/java/org/acme/rest/json/`.

## Current Stack

- Java 21
- Quarkus 3.34.3
- `quarkus-rest-jackson`
- `quarkus-hibernate-orm-panache`
- `quarkus-jdbc-h2`
- `quarkus-hibernate-validator`
- `quarkus-smallrye-openapi`
- `quarkus-smallrye-health`
- `quarkus-micrometer-registry-prometheus`
- `quarkus-opentelemetry`
- `quarkus-info`
- `quarkus-resteasy-problem`
- MapStruct 1.6.3
- Lombok 1.18.36
- JUnit 5 and REST Assured

## Observability Notes

- Swagger UI is available at `/q/swagger-ui`.
- Health endpoints are available at `/q/health`, `/q/health/ready`, and `/q/health/live`.
- Metrics are available at `/q/metrics`.
- `LoggingFilter` propagates `X-Request-Id`.
- Logs include `requestId`, `traceId`, and `spanId`.
- Business metrics include `pet_create_total` and `user_create_total`.

Local observability support is available through Docker Compose, including OTEL Collector and Jaeger.

Relevant environment variables include:

- `OTEL_EXPORTER_OTLP_ENDPOINT`
- `OTEL_EXPORTER_OTLP_PROTOCOL`
- `OTEL_TRACES_SAMPLER`
- `OTEL_TRACES_SAMPLER_ARG`
- `DEPLOYMENT_ENVIRONMENT`
- `OTEL_EXPORTER_OTLP_ENABLED`

## Change Hygiene

- Update related tests in the same change when modifying resources, DTOs, validation rules, or persistence behavior.
- Avoid introducing new dependencies unless they are necessary for the task.
- Keep README and runnable commands accurate if build, Docker, observability, or local workflow behavior changes.
- Verify formatting before finishing with `./mvnw spotless:check`.
