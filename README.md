# Quarkus MS Demo

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=alert_status)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=coverage)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=bugs)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=vulnerabilities)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=code_smells)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=reliability_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=security_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=sqale_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)

REST API built with Quarkus, inspired by the Swagger Petstore contract, organized in simple layers to facilitate learning, evolution, and testing.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file.

## Educational Scope

- This project is educational and does not address production security concerns (authentication, authorization, or hardening).
- The project is database-agnostic; the in-memory H2 database is just the default configuration for local development and quick tests.

The project exposes three main areas:

- `pet`: pet registration and queries persisted in H2
- `store`: inventory and orders persisted in H2
- `user`: user operations with persistence in H2 using Panache

## Stack

- Java 21
- Quarkus 3.34.3
- REST with Jackson
- SmallRye OpenAPI + Swagger UI
- Hibernate ORM Panache
- H2 in-memory
- SmallRye Health
- Micrometer
- Quarkus Info
- OpenTelemetry (OTLP tracing)
- RESTEasy Problem (RFC 7807)
- JUnit + RestAssured
- Spotless with Google Java Format

## How to Run

Prerequisites:

- JDK 21+

Start the application in development mode:

```sh
./run.sh
```

Or, if you prefer, directly with Maven Wrapper:

```sh
./mvnw quarkus:dev
```

With the application running:

- API: `http://localhost:8080`
- OpenAPI: `http://localhost:8080/q/openapi`
- Swagger UI: `http://localhost:8080/q/swagger-ui`
- Dev UI: `http://localhost:8080/q/dev-ui`
- Health: `http://localhost:8080/q/health`
- Readiness: `http://localhost:8080/q/health/ready`
- Liveness: `http://localhost:8080/q/health/live`
- Metrics: `http://localhost:8080/q/metrics`
- Info: `http://localhost:8080/q/info`

To inspect the in-memory H2 database during development, use:

- `http://localhost:8080/q/dev-ui`

In the Dev UI, open the datasource/H2 section to browse tables and run SQL queries. The project is already configured with `%dev.quarkus.datasource.dev-ui.allow-sql=true` to allow this in the development environment.

## Docker Compose (Full Local Stack)

To run the application in a container with full observability support (OTEL Collector + Jaeger), use Docker Compose:

```sh
# Prerequisite: build the application and Docker image
./mvnw package -DskipTests

# Or use the convenience script
./run-build-prod.sh

# Start the full local stack (application + OTEL Collector + Jaeger)
docker-compose up

# In another terminal, view traces in real time:
# Jaeger UI: http://localhost:16686
# Swagger UI: http://localhost:8080/q/swagger-ui
# Health: http://localhost:8080/q/health
# Metrics: http://localhost:8080/q/metrics
```

**What is included in the Docker Compose stack:**

- **app** (Quarkus JVM): port 8080
  - OTEL endpoint: `http://otel-collector:4317`
  - Automatic health check
  - Configured to export OTLP traces

- **otel-collector** (OpenTelemetry Collector): ports 4317 (gRPC OTLP), 4318 (HTTP OTLP), 8888 (Prometheus metrics)
  - Receives spans from the application
  - Exports to Jaeger
  - Optional debug logging for troubleshooting
  - Configured memory limits

- **jaeger** (Distributed Tracing Backend): ports 6831 (UDP), 16686 (UI)
  - Trace visualization UI: <http://localhost:16686>
  - Receives spans from OTEL Collector

**Shutdown:**

```sh
docker-compose down

# Remove volumes (clear stack data):
docker-compose down -v
```

**Troubleshooting:**

- If the app container fails to start, confirm that `./mvnw package` completed successfully
- If Jaeger is not receiving traces, verify that the OTEL Collector health check passed (`docker-compose logs otel-collector`)
- To enable detailed OTEL Collector logs, edit `otel-collector-config.yaml` and set `loglevel: debug` under `exporters.logging`

## Observability

The project has native OpenTelemetry support via the official Quarkus extension, with OTLP tracing and correlation of `traceId` and `spanId` in logs.

Complementary operational extensions are also included:

- `quarkus-smallrye-health`: standard Quarkus health checks
- `quarkus-micrometer`: metrics instrumentation
- `quarkus-micrometer-registry-prometheus`: metrics exposure at `/q/metrics`
- `quarkus-info`: build and git metadata at a dedicated endpoint

Main configuration settings:

- `OTEL_EXPORTER_OTLP_ENDPOINT`: OTLP collector endpoint. Default: `http://localhost:4317`
- `OTEL_EXPORTER_OTLP_PROTOCOL`: OTLP protocol. Default: `grpc`
- `OTEL_EXPORTER_OTLP_HEADERS`: extra headers for collector authentication
- `OTEL_TRACES_SAMPLER`: OTel sampler. Default: `parentbased_traceidratio`
- `OTEL_TRACES_SAMPLER_ARG`: sampling rate when applicable. Default: `1.0`
- `DEPLOYMENT_ENVIRONMENT`: `deployment.environment` attribute sent in OTel resources. Default: `dev`
- `OTEL_EXPORTER_OTLP_ENABLED`: in `dev`, controls whether OTLP export is active. Default: `false`

Best practices adopted:

- no Java agent used, leveraging Quarkus native instrumentation
- tracing enabled with environment-configurable OTLP export
- logs with trace correlation to facilitate troubleshooting
- OTel SDK disabled in tests to keep the suite stable and quiet
- metrics follow Micrometer, which is the Quarkus-recommended approach for this signal

Minimum observability contract:

- every HTTP response returns `X-Request-Id`
- if the client sends `X-Request-Id`, the value is preserved and reused
- if the client does not send `X-Request-Id`, the application generates an identifier automatically
- logs include `requestId`, `traceId`, `spanId`, and `sampled`
- each request generates a start log and an end log with `method`, `path`, `status`, `durationMs`, and `remoteIp`
- minimum business metrics exposed at `/q/metrics`, including `pet_create_total` and `user_create_total`

This creates a simple correlation convention between the client, application logs, and distributed tracing, sufficient for quick troubleshooting without adding architectural complexity to the project.

How to use:

- `GET /q/health`: aggregated application health view
- `GET /q/health/live`: liveness probe
- `GET /q/health/ready`: readiness probe
- `GET /q/metrics`: application and infrastructure metrics
- `GET /q/info`: build and git information when available

## Useful Scripts

- `./run.sh`: starts the application in dev mode
- `./run-check.sh`: runs tests and validates formatting
- `./run-spotless-apply.sh`: automatically fixes Java formatting
- `./run-build-prod.sh`: generates the production build with `clean verify`
- `./run-docker.sh`: builds the JVM Docker image and runs the application in a container

Windows equivalents:

- `run.cmd`
- `run-check.cmd`
- `run-spotless-apply.cmd`
- `run-build-prod.cmd`
- `run-docker.cmd`

Production build:

```sh
./run-build-prod.sh
```

Run in Docker:

```sh
./run-docker.sh
```

Optional variables for the Docker script:

- `IMAGE_NAME`: image name. Default: `quarkus-ms-demo:jvm`
- `CONTAINER_NAME`: container name. Default: `quarkus-ms-demo`
- `HOST_PORT`: port published on the host. Default: `8080`

## Main Endpoints

### Pet

- `POST /pet`
- `PUT /pet`
- `GET /pet/findByStatus`
- `GET /pet/findByTags`
- `GET /pet/{petId}`
- `POST /pet/{petId}`
- `DELETE /pet/{petId}`
- `POST /pet/{petId}/uploadImage`

### Store

- `GET /store/inventory`
- `POST /store/order`
- `GET /store/order/{orderId}`
- `DELETE /store/order/{orderId}`

### User

- `GET /user`
- `GET /user/examples/named-query?status=1`
- `GET /user/examples/named-native-query?emailDomain=example.com`
- `GET /user/examples/criteria?usernamePrefix=seed-user&status=1&emailDomain=example.com`
- `POST /user`
- `POST /user/createWithArray`
- `POST /user/createWithList`
- `GET /user/{username}`
- `PUT /user/{username}`
- `DELETE /user/{username}`

## Persistence and Initial Data

The project uses H2 in-memory:

- datasource: `jdbc:h2:mem:default`
- schema strategy: `drop-and-create`

When the application starts, initial users are loaded by [import.sql](src/main/resources/import.sql):

- `seed-user-1`
- `seed-user-2`

The `pet`, `store`, and `user` domains use H2 in-memory with schema recreated on every boot and data seeded by [import.sql](src/main/resources/import.sql).

## Project Structure

```text
src/main/java/org/acme
├── pet
│   ├── persistence
│   ├── resources
│   │   └── dtos
│   └── services
│       └── mappers
├── store
│   ├── persistence
│   ├── resources
│   │   └── dtos
│   └── services
│       └── mappers
├── user
│   ├── persistence
│   ├── resources
│   │   └── dtos
│   └── services
│       └── mappers
└── shared
```

Package summary:

- `pet`: explicit separation between HTTP resource, DTOs in `resources/dtos`, service, mappers in `services/mappers`, and JPA persistence
- `store`: follows the same organization as `pet`, with DTOs under `resources/dtos` and mappers under `services/mappers`
- `user`: follows the same organization as `pet`, with DTOs under `resources/dtos` and mappers under `services/mappers`
- `shared`: filters and cross-cutting components, such as request logging

## Database Access Examples

Project rule for JPA queries:

- use `@NamedQuery`, `@NamedNativeQuery`, or `Criteria API`
- do not use `EntityManager.createQuery` with JPQL strings

In addition to Panache, the project also contains explicit examples of other JPA approaches in the `user` domain:

- `NamedQuery`: `GET /user/examples/named-query?status=1`
- `NamedNativeQuery`: `GET /user/examples/named-native-query?emailDomain=example.com`
- `Criteria API`: `GET /user/examples/criteria?usernamePrefix=seed-user&status=1&emailDomain=example.com`

Code examples:

- `findByStatusNamedQuery` in [UserRepository.java](src/main/java/org/acme/user/persistence/UserRepository.java)
- `findByEmailDomainNativeQuery` in [UserRepository.java](src/main/java/org/acme/user/persistence/UserRepository.java)
- `findByCriteria` in [UserRepository.java](src/main/java/org/acme/user/persistence/UserRepository.java)

These examples are implemented in [User.java](src/main/java/org/acme/user/persistence/User.java), [UserRepository.java](src/main/java/org/acme/user/persistence/UserRepository.java), and [UserResource.java](src/main/java/org/acme/user/resources/UserResource.java).

## Quality and Tests

To run the test suite locally:

```sh
./mvnw test
```

The tests cover:

- `pet` endpoints
- `store` endpoints
- `user` endpoints
- OpenAPI document and Swagger UI

## Notes

- The OpenAPI documentation published by the application uses the title `Swagger Petstore`.
- HTTP errors produced by resources follow the `application/problem+json` format via the `quarkus-resteasy-problem` extension.
- There are legacy static files in `src/main/resources/META-INF/resources`, but the current focus of the project is the REST API.
- The project recommends the VS Code extension `redhat.vscode-quarkus` in `.vscode/extensions.json`.
