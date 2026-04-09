# Project Review: quarkus-ms-demo vs quarkus-super-heroes

> Analysis date: 2026-04-09
> Reference project: [quarkus-super-heroes](https://github.com/quarkusio/quarkus-super-heroes) (Quarkus 3.34.2 / Java 21)
> This project: quarkus-ms-demo (Quarkus 3.34.3 / Java 21)

---

## What this project already does well

| Area | Status |
| --- | --- |
| Quarkus version | Up to date (3.34.3 vs 3.34.2 in super-heroes) |
| Layered architecture (resource / service / persistence) | Consistent across all domains |
| MapStruct for DTO mapping | Same version (1.6.3) |
| OpenTelemetry tracing (OTLP, no Java agent) | Identical stack |
| Micrometer + Prometheus metrics | Identical stack |
| SmallRye Health + Info | Identical stack |
| RFC 7807 error responses | Present (`quarkus-resteasy-problem`) |
| Bean Validation on request DTOs | Present |
| Spotless / Google Java Format enforcement | Present (super-heroes does NOT use this) |
| JaCoCo coverage reports | Present (`jacoco-maven-plugin 0.8.12`) |
| GitHub Actions CI | Present (`ci.yml`) |
| Docker Compose observability stack (OTEL Collector + Jaeger) | Present |
| Structured logging with `X-Request-Id` correlation | Present (`LoggingFilter`) |
| Pagination envelope (`PagedResponse`) | Present |
| Qute home page | Present (`UiHomeResource`) |
| Windows script equivalents (`.cmd`) | Present |

---

## Gaps identified: what super-heroes uses that this project does not

### 1. Testing

| Gap | super-heroes approach | Educational value |
| --- | --- | --- |
| **CDI-aware mocking** | `quarkus-junit-mockito` (`@InjectMock`) | Replaces CDI beans in tests without restarting the container |
| **Panache mocking** | `quarkus-panache-mock` | Mocks static Panache methods without hitting the database |
| **Fluent assertions** | `assertj-core` | More readable assertions than standard JUnit |
| **Parameterized tests** | `junit-jupiter-params` | `@MethodSource`, `@CsvSource` — reduces test duplication |
| **WireMock** | `org.wiremock:wiremock` | HTTP stub server for REST client and external service tests |
| **Contract testing** | `quarkus-pact-provider` + `quarkus-pact-consumer` | Validates API contracts between consumer and provider |
| **Browser/UI testing** | `quarkus-playwright` | End-to-end browser tests for Swagger UI or custom frontends |
| **Async assertion** | `org.awaitility:awaitility` | Polls conditions with timeout — needed for async/event-driven flows |
| **Tree test output** | `maven-surefire-junit5-tree-reporter` | Structured, readable test output in CI logs |
| **Coverage threshold** | JaCoCo `<rule>` with minimum ratio | Fails the build when coverage drops below a threshold |

> **Note:** `assertj-core`, `junit-jupiter-params`, and `wiremock` are already present in this project's `pom.xml` but are not yet widely used across all test classes.

---

### 2. REST and HTTP

| Gap | super-heroes approach | Educational value |
| --- | --- | --- |
| **Declarative REST client** | `quarkus-rest-client-jackson` (MicroProfile REST Client) | Typed HTTP client interface — common pattern in microservice communication |
| **Fault tolerance** | `quarkus-smallrye-fault-tolerance` | `@Retry`, `@CircuitBreaker`, `@Timeout`, `@Fallback` on outgoing calls |

---

### 3. Messaging and Reactive

| Gap | super-heroes approach | Educational value |
| --- | --- | --- |
| **Reactive Messaging / Kafka** | `quarkus-messaging-kafka` | Event-driven communication between services |
| **Avro + Schema Registry** | `quarkus-apicurio-registry-avro` | Schema evolution and type-safe message serialization |
| **WebSocket push** | `quarkus-websockets-next` | Real-time updates to browser clients |
| **Reactive ORM** | `quarkus-hibernate-reactive-panache` | Non-blocking persistence with Mutiny |

---

### 4. Persistence

| Gap | super-heroes approach | Educational value |
| --- | --- | --- |
| **Production database** | PostgreSQL (reactive + blocking) and MongoDB | Demonstrates real database configuration beyond H2 in-memory |
| **Schema migration** | `quarkus-liquibase-mongodb` (MongoDB) | Schema evolution — Flyway/Liquibase is the standard for production |
| **JDBC OTel instrumentation** | `opentelemetry-jdbc` | Adds spans for individual SQL statements in traces |

> **Note:** H2 in-memory is appropriate for an educational project. The gap is that the project does not demonstrate how to switch to a real database via profiles or environment variables.

---

### 5. Deployment and Cloud

| Gap | super-heroes approach | Educational value |
| --- | --- | --- |
| **Kubernetes manifests** | `quarkus-kubernetes` + `quarkus-openshift` + `quarkus-minikube` | Generates deployment manifests automatically from application metadata |
| **Container image build** | `quarkus-container-image-docker` | Builds and tags Docker images as part of the Maven lifecycle |
| **Service discovery** | `quarkus-smallrye-stork` | Pluggable client-side load balancing and service discovery |

---

### 6. Observability

| Gap | super-heroes approach | Educational value |
| --- | --- | --- |
| **JDBC span instrumentation** | `opentelemetry-jdbc` | SQL queries appear as child spans in distributed traces — already listed under persistence above |

This project's observability stack is otherwise equivalent to super-heroes.

---

### 7. Code style

| Aspect | This project | super-heroes |
| --- | --- | --- |
| **Formatting tool** | Spotless + Google Java Format | None |
| **Lombok** | Yes (`@RequiredArgsConstructor`) | No — uses plain CDI constructor injection |

This project is stricter on formatting. The absence of Lombok in super-heroes is a deliberate choice: Quarkus CDI handles injection via the `@Inject`-annotated constructor automatically, so Lombok is not needed. However, for this educational project, Lombok reduces boilerplate and is a commonly requested library.

---

## Prioritized recommendations

The following are ordered by educational value and implementation effort.

### High value / low effort

1. **Add `assertj-core` usage across all test classes** — the dependency exists but is underused. Replace `assertEquals` with AssertJ `assertThat` chains.

2. **Add `@ParameterizedTest` examples** — `junit-jupiter-params` is present. Add at least one parameterized test to demonstrate the pattern.

3. **Add a JaCoCo minimum coverage rule** — configure a `<rule>` in `jacoco-maven-plugin` to enforce a coverage floor (e.g., 70% line coverage). This teaches the concept of quality gates.

4. **Add `quarkus-junit-mockito` (`@InjectMock`)** — demonstrates CDI-aware mocking, which is different from plain Mockito and commonly needed in Quarkus apps.

5. **Add `quarkus-panache-mock`** — demonstrates how to mock Panache static methods in tests without a real database.

6. **Document the H2 → PostgreSQL switch** — add a section or profile example showing how `application.properties` changes when switching from H2 to a real database. This bridges the gap between the demo and production usage.

### Medium value / medium effort

7. **Add `quarkus-rest-client-jackson` example** — implement one domain calling another (or a mock external service) via a typed MicroProfile REST Client interface. Demonstrates the most common inter-service communication pattern.

8. **Add `quarkus-smallrye-fault-tolerance` to the REST client** — annotate the client calls with `@Retry` and `@Fallback`. Naturally pairs with the REST client addition above.

9. **Add `opentelemetry-jdbc`** — one line in `pom.xml` adds SQL spans to traces, giving a much richer observability picture for free.

10. **Add WireMock-based tests** — the dependency exists. Add at least one test that stubs an external HTTP call and verifies retry/fallback behavior.

### Lower priority / higher effort

11. **Add a Kafka messaging example** — `quarkus-messaging-kafka` with an in-memory channel for tests (`smallrye-reactive-messaging-in-memory`). Demonstrates event-driven patterns with minimal infrastructure.

12. **Add `quarkus-kubernetes`** — generates `kubernetes.yml` / `openshift.yml` manifests automatically. Teaches developers what a Quarkus deployment descriptor looks like.

13. **Add Pact contract testing** — demonstrates consumer-driven contract testing, which is valuable in microservice environments where API compatibility between teams must be maintained.

---

## What this project does that super-heroes does not

| This project | super-heroes |
| --- | --- |
| Spotless / Google Java Format enforcement | No formatting enforcement |
| `LoggingFilter` with `X-Request-Id` propagation (explicit implementation) | Uses OTel trace correlation only |
| `ListResponse<T>` / `PagedResponse<T>` standardized envelopes | Varied per module |
| Windows `.cmd` equivalents for all scripts | Unix only |
| Multilingual README (EN / PT-BR / ES) | Single language |
| Makefile for common tasks | No Makefile |
| Qute home page at `/` with dev tool links | No home page |
| `ApplicationLifecycle` startup/shutdown logging | Not present |
| `RestEndpointLivenessHealthCheck` custom liveness check | Uses default health checks only |
| Explicit JPA query examples (NamedQuery, NamedNativeQuery, Criteria API) | Uses Panache or reactive patterns only |

---

## Summary

This project is a well-structured, up-to-date educational Quarkus application. The main gaps relative to super-heroes are in **advanced testing patterns** (CDI mocks, Panache mocks, contract testing, browser testing), **inter-service communication** (REST client + fault tolerance), and **cloud deployment** (Kubernetes manifests). These are all reasonable next steps that would increase the educational coverage without changing the project's single-application nature.
