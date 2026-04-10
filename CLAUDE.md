# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Development mode (hot reload)
./run.sh
# or: ./mvnw quarkus:dev

# Run tests + formatting check
./run-check.sh
# or: ./mvnw test spotless:check

# Auto-fix formatting
./run-spotless-apply.sh
# or: ./mvnw spotless:apply

# Production build
./run-build-prod.sh
# or: ./mvnw clean verify

# Build Docker image and run container
./run-docker.sh

# Run a single test class
./mvnw test -Dtest=PetResourceTest

# Run integration tests (against packaged JVM binary)
./mvnw verify

# Makefile shortcuts (Unix/macOS)
make dev
make check
make fmt
make build
make docker
```

Windows equivalents: `run.cmd`, `run-check.cmd`, `run-spotless-apply.cmd`, `run-build-prod.cmd`, `run-docker.cmd`

## Architecture

Quarkus 3.x application (Java 21) implementing a REST API modeled after the Swagger Petstore contract. Organized in domain-driven packages with layered architecture.

### Package Structure

```text
src/main/java/org/acme/
├── pet/
│   ├── persistence/         # JPA entities: Pet, Category, Tag + PetRepository
│   ├── resources/           # JAX-RS: PetResource
│   │   └── dtos/            # Records: PetRequest/Response, CategoryRequest/Response, TagRequest/Response
│   └── services/            # PetService
│       └── mappers/         # PetMapper (MapStruct)
├── store/
│   ├── persistence/         # JPA entities: Order + OrderRepository
│   ├── resources/           # JAX-RS: StoreResource
│   │   └── dtos/            # Records: OrderRequest/Response, InventoryResponse
│   └── services/            # StoreService
│       └── mappers/         # OrderMapper (MapStruct)
├── user/
│   ├── persistence/         # JPA entity: User + UserRepository (Panache)
│   ├── resources/           # JAX-RS: UserResource
│   │   └── dtos/            # Records: UserRequest/Response
│   └── services/            # UserService
│       └── mappers/         # UserMapper (MapStruct)
└── shared/
    ├── LoggingFilter.java   # JAX-RS @Provider: request/response logging + X-Request-Id propagation
    └── ApiResponse.java     # Shared response record
```

### Test Structure

```text
src/test/java/org/acme/
├── pet/resources/           # PetResourceTest (@QuarkusTest), PetResourceIT (@QuarkusIntegrationTest)
├── store/resources/         # StoreResourceTest, StoreResourceIT
├── user/resources/          # UserResourceTest, UserResourceIT
└── rest/json/               # OpenApiResourceTest, OpenApiResourceIT (cross-cutting)
```

### Endpoints

**Pet** (`/pet`)

- `POST /pet`, `PUT /pet`
- `GET /pet/findByStatus`, `GET /pet/findByTags`
- `GET /pet/{petId}`, `POST /pet/{petId}`, `DELETE /pet/{petId}`
- `POST /pet/{petId}/uploadImage`

**Store** (`/store`)

- `GET /store/inventory`
- `POST /store/order`, `GET /store/order/{orderId}`, `DELETE /store/order/{orderId}`

**User** (`/user`)

- `GET /user`, `POST /user`, `POST /user/createWithArray`, `POST /user/createWithList`
- `GET /user/{username}`, `PUT /user/{username}`, `DELETE /user/{username}`
- `GET /user/examples/named-query`, `GET /user/examples/named-native-query`, `GET /user/examples/criteria`

### Key Conventions

- **DTOs**: Java records in `resources/dtos/`; Bean Validation on request DTOs (`@NotBlank`, `@NotNull`, `@Valid`, etc.)
- **Mapping**: MapStruct mappers in `services/mappers/`; no direct entity exposure in responses
- **Injection**: Lombok `@RequiredArgsConstructor` for constructor injection
- **Errors**: RFC 7807 `application/problem+json` via `quarkus-resteasy-problem`; throw JAX-RS exceptions (`NotFoundException`, etc.)
- **Persistence**: Hibernate ORM Panache + H2 in-memory (`drop-and-create`); JPA queries via `@NamedQuery`, `@NamedNativeQuery`, or Criteria API — never `createQuery` with inline JPQL strings
- **Formatting**: Spotless (Google Java Format) — runs automatically in the `validate` phase on every build
- **Observability**: `X-Request-Id` header propagated by `LoggingFilter`; logs include `requestId`, `traceId`, `spanId`; business metrics `pet_create_total` and `user_create_total` via Micrometer

### Stack

- Java 21, Quarkus 3.34.3
- REST: `quarkus-rest-jackson`
- Persistence: `quarkus-hibernate-orm-panache`, `quarkus-jdbc-h2`
- Validation: `quarkus-hibernate-validator`
- API docs: `quarkus-smallrye-openapi` (Swagger UI at `/q/swagger-ui`)
- Health: `quarkus-smallrye-health` (`/q/health`, `/q/health/ready`, `/q/health/live`)
- Metrics: `quarkus-micrometer-registry-prometheus` (`/q/metrics`)
- Tracing: `quarkus-opentelemetry` (OTLP export, configurable via env vars)
- Info: `quarkus-info` (`/q/info`)
- Error handling: `quarkus-resteasy-problem` (RFC 7807)
- Mapping: MapStruct 1.6.3, Lombok 1.18.36
- Testing: JUnit 5, REST-Assured, `@QuarkusTest` / `@QuarkusIntegrationTest`

### Observability

Local full stack via Docker Compose (`docker-compose.yml`):

- **OTEL Collector** — ports 4317 (gRPC), 4318 (HTTP), 8888 (Prometheus metrics)
- **Jaeger UI** — `http://localhost:16686`
- Config: `otelcol-config.yaml`

Key env vars:

- `OTEL_EXPORTER_OTLP_ENDPOINT` (default: `http://localhost:4317`)
- `OTEL_EXPORTER_OTLP_PROTOCOL` (default: `grpc`)
- `OTEL_TRACES_SAMPLER` / `OTEL_TRACES_SAMPLER_ARG`
- `DEPLOYMENT_ENVIRONMENT` (default: `dev`)
- `OTEL_EXPORTER_OTLP_ENABLED` — set to `false` in dev by default; OTel SDK fully disabled in tests
