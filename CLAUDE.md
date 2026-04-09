# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Development mode (hot reload)
./mvnw quarkus:dev

# Build (JVM)
./mvnw package

# Build (native image - requires GraalVM)
./mvnw package -Dnative

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=FruitResourceTest

# Run integration tests (against packaged native binary)
./mvnw verify -Dnative

# Check formatting (runs automatically on every build via validate phase)
./mvnw spotless:check

# Auto-fix formatting
./mvnw spotless:apply

# Docker - JVM
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/rest-json-quickstart-jvm .
docker run -i --rm -p 8080:8080 quarkus/rest-json-quickstart-jvm

# Docker - Native
docker build -f src/main/docker/Dockerfile.native -t quarkus/rest-json-quickstart .
docker run -i --rm -p 8080:8080 quarkus/rest-json-quickstart
```

## Architecture

Single Quarkus 3.x application (Java 21) demonstrating REST JSON APIs. Based on the [Quarkus REST JSON guide](https://quarkus.io/guides/rest-json).

**Endpoints:**
- `GET/POST/DELETE /fruits` — mutable fruit collection (in-memory `LinkedHashMap`)
- `GET /legumes` — read-only legume collection (in-memory `LinkedHashSet`)

**Package:** `org.acme.rest.json`

- `FruitResource` / `LegumeResource` — JAX-RS resources with `@Produces`/`@Consumes` JSON
- `Fruit` / `Legume` — plain POJOs; `Legume` has `@RegisterForReflection` for GraalVM native image support
- `LoggingFilter` — `@Provider` JAX-RS `ContainerRequestFilter` that logs method, path, and client IP for every request

**Frontend:** Static AngularJS (v1.4.8) HTML pages served from `META-INF/resources/` — `fruits.html` supports full CRUD, `legumes.html` is read-only.

**Testing:** `@QuarkusTest` for unit tests, `@QuarkusIntegrationTest` for native integration tests (runs against the packaged binary). Uses REST-Assured + JUnit 5.
