# Copilot Instructions

This repository is a Quarkus 3 REST example built with Maven and Java 17.

## Stack and Structure

- Use Java 17 language features only.
- Build and run with the Maven wrapper: `./mvnw`.
- Main application code lives under `src/main/java/org/acme/rest/json`.
- HTTP resources use Jakarta REST annotations and are exposed as simple Quarkus endpoints.
- Static web assets live under `src/main/resources/META-INF/resources`.
- Do not edit generated files under `target/`.

## Coding Guidelines

- Keep package names under `org.acme.rest.json` unless there is a clear reason to introduce a new package.
- Follow the existing style: small resource classes, straightforward fields, and minimal abstraction.
- Prefer focused changes over broad refactors.
- Preserve the existing API behavior unless the task explicitly requires a contract change.
- When adding or changing endpoints, keep request and response payloads simple and compatible with Jackson serialization.

## Testing Expectations

- Add or update tests for behavior changes.
- Use `@QuarkusTest` for application tests and RestAssured for HTTP assertions, matching the existing test style.
- Keep test classes alongside the existing tests under `src/test/java/org/acme/rest/json`.
- If native-specific behavior is added, keep the existing integration test pattern in mind.

## Useful Commands

- Start dev mode: `./mvnw quarkus:dev`
- Run unit tests: `./mvnw test`
- Run the full verification flow when needed: `./mvnw verify`

## Change Hygiene

- Update related tests in the same change when modifying resources or payload models.
- Avoid introducing new dependencies unless they are necessary for the task.
- Keep README and runnable commands accurate if build or run behavior changes.