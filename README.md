# Quarkus MS Demo

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=alert_status)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=coverage)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=bugs)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=vulnerabilities)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=code_smells)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=reliability_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=security_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=sqale_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)

Also available in: [Português](README.pt-br.md) · [Español](README.es.md)

Educational REST API built with **Java 21 + Quarkus 3.x**, inspired by the Swagger Petstore contract. The goal is to demonstrate good practices for layered architecture, persistence, observability, and testing in a modern REST API.

> This project is educational and does not address production security concerns (authentication, authorization, or hardening).

## License

MIT License. See [LICENSE](LICENSE).

---

## Package Structure

The project is organized by domain with explicit separation of concerns across layers:

```text
src/main/java/org/acme/
├── pet/
│   ├── persistence/     # JPA entity + Repository
│   ├── resources/       # JAX-RS Resource (controller)
│   │   └── dtos/        # Input/output records (Request/Response)
│   └── services/        # Business logic
│       └── mappers/     # Entity ↔ DTO conversion via MapStruct
├── store/               # Same pattern as pet
├── user/                # Same pattern as pet
└── shared/
    ├── ApiResponse.java         # Generic response envelope
    ├── ListResponse.java        # Envelope for simple lists
    ├── LoggingFilter.java       # JAX-RS filter: logging and X-Request-Id
    └── pagination/              # PagedResponse, PageMetadata, SortMetadata
```

Each domain (`pet`, `store`, `user`) is self-contained. The `shared` layer holds reusable response contracts and cross-cutting filters.

---

## List Response Format

The project standardizes two envelopes for collection responses:

**Simple list** — no pagination:

```json
{
  "items": [ ... ]
}
```

**Paginated list** — with page and sort metadata:

```json
{
  "items": [ ... ],
  "page": {
    "number": 0,
    "size": 10,
    "totalElements": 42,
    "totalPages": 5,
    "first": true,
    "last": false,
    "hasNext": true,
    "hasPrevious": false
  },
  "sort": {
    "by": "username",
    "direction": "ASC"
  }
}
```

Never return bare arrays in the response body. Always use `ListResponse<T>` or `PagedResponse<T>` from the `shared` layer.

---

## Persistence

- H2 in-memory database (`jdbc:h2:mem:default`), recreated on every boot (`drop-and-create`)
- Initial data loaded from [import.sql](src/main/resources/import.sql)
- JPA queries must use `@NamedQuery`, `@NamedNativeQuery`, or the Criteria API — never `createQuery` with inline JPQL strings
- The `user` domain contains explicit examples of all three approaches, accessible via `/user/examples/*` endpoints

---

## Dependencies and Plugins

### Quarkus Extensions

| Extension | Purpose |
| --- | --- |
| [quarkus-rest](https://quarkus.io/guides/rest) + [quarkus-rest-jackson](https://quarkus.io/guides/rest-json) | Reactive REST server with Jackson serialization |
| [quarkus-smallrye-openapi](https://quarkus.io/guides/openapi-swaggerui) | Auto-generates the OpenAPI contract and Swagger UI at `/q/swagger-ui` |
| [quarkus-hibernate-orm-panache](https://quarkus.io/guides/hibernate-orm-panache) | ORM with simplified Active Record and Repository patterns |
| [quarkus-jdbc-h2](https://quarkus.io/guides/datasource) | JDBC driver for the H2 in-memory database |
| [quarkus-hibernate-validator](https://quarkus.io/guides/validation) | Bean Validation (`@NotBlank`, `@NotNull`, `@Valid`) on request DTOs |
| [quarkus-smallrye-health](https://quarkus.io/guides/smallrye-health) | Health checks at `/q/health`, `/q/health/live`, `/q/health/ready` |
| [quarkus-micrometer](https://quarkus.io/guides/micrometer) + [quarkus-micrometer-registry-prometheus](https://quarkus.io/guides/micrometer) | Application metrics exposed at `/q/metrics` (Prometheus format) |
| [quarkus-info](https://quarkus.io/guides/info) | Build and git metadata at `/q/info` |
| [quarkus-opentelemetry](https://quarkus.io/guides/opentelemetry) | Distributed tracing via OTLP without a Java agent; correlates `traceId`/`spanId` in logs |

### Libraries

| Library | Purpose |
| --- | --- |
| [MapStruct 1.6](https://mapstruct.org/) | Compile-time mapper generation between entities and DTOs |
| [Lombok 1.18](https://projectlombok.org/) | Reduces boilerplate (`@RequiredArgsConstructor` for constructor injection) |
| [quarkus-resteasy-problem](https://github.com/quarkiverse/quarkus-resteasy-problem) | HTTP errors in RFC 7807 format (`application/problem+json`) |

### Maven Plugins

| Plugin | Purpose |
| --- | --- |
| [Spotless + Google Java Format](https://github.com/diffplug/spotless) | Automatic code formatting; fails the build if code is not formatted |
| [quarkus-maven-plugin](https://quarkus.io/guides/maven-tooling) | Quarkus lifecycle: `quarkus:dev`, `package`, native build |

### Testing

| Tool | Purpose |
| --- | --- |
| [quarkus-junit](https://quarkus.io/guides/getting-started-testing) | `@QuarkusTest` (unit/integration on JVM) and `@QuarkusIntegrationTest` (against packaged binary) |
| [REST-assured](https://rest-assured.io/) | Fluent DSL for testing HTTP endpoints |

---

## How to Run

```sh
# Dev mode (hot reload)
./run.sh

# Tests + formatting check
./run-check.sh

# Auto-fix formatting
./run-spotless-apply.sh

# Production build
./run-build-prod.sh

# Build Docker image and run container
./run-docker.sh
```

Windows: use the `.cmd` equivalents.

With the application running:

- Swagger UI: `http://localhost:8080/q/swagger-ui`
- Dev UI (H2 console): `http://localhost:8080/q/dev-ui`
- Health: `http://localhost:8080/q/health`
- Metrics: `http://localhost:8080/q/metrics`
- Info: `http://localhost:8080/q/info`

---

## Observability

Every request receives an `X-Request-Id` header (preserved from the client or generated automatically). Logs include `requestId`, `traceId`, and `spanId`. Business metrics (`pet_create_total`, `user_create_total`) are exposed via Micrometer.

To run the full observability stack (OTEL Collector + Jaeger):

```sh
./mvnw package -DskipTests
docker-compose up
# Jaeger UI: http://localhost:16686
```
