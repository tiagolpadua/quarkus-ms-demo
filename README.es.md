# Quarkus MS Demo

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=alert_status)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=coverage)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=bugs)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=vulnerabilities)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=code_smells)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=reliability_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=security_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=sqale_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)

También disponible en: [English](README.md) · [Português](README.pt-br.md)

API REST educativa construida con **Java 21 + Quarkus 3.x**, inspirada en el contrato Swagger Petstore. El objetivo es demostrar buenas prácticas de arquitectura en capas, persistencia, observabilidad y pruebas en una API REST moderna.

> Este proyecto es educativo y no aborda aspectos de seguridad en producción (autenticación, autorización o hardening).

## Licencia

Licencia MIT. Consulte el archivo [LICENSE](LICENSE).

---

## Estructura de Paquetes

El proyecto se organiza por dominio con separación explícita de responsabilidades en capas:

```text
src/main/java/org/acme/
├── pet/
│   ├── persistence/     # Entidad JPA + Repository
│   ├── resources/       # JAX-RS Resource (controlador)
│   │   └── dtos/        # Records de entrada y salida (Request/Response)
│   └── services/        # Lógica de negocio
│       └── mappers/     # Conversión entidad ↔ DTO via MapStruct
├── store/               # Mismo patrón que pet
├── user/                # Mismo patrón que pet
└── shared/
    ├── ApiResponse.java         # Envelope genérico de respuesta
    ├── ListResponse.java        # Envelope para listas simples
    ├── LoggingFilter.java       # Filtro JAX-RS: logs y X-Request-Id
    └── pagination/              # PagedResponse, PageMetadata, SortMetadata
```

Cada dominio (`pet`, `store`, `user`) es autocontenido. La capa `shared` centraliza los contratos de respuesta reutilizables y los filtros transversales.

---

## Formato de Respuesta para Listas

El proyecto estandariza dos envelopes para devolver colecciones:

**Lista simple** — sin paginación:

```json
{
  "items": [ ... ]
}
```

**Lista paginada** — con metadatos de página y ordenación:

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

Nunca devuelva arrays directamente en el cuerpo de la respuesta. Utilice siempre `ListResponse<T>` o `PagedResponse<T>` de la capa `shared`.

---

## Persistencia

- Base de datos H2 en memoria (`jdbc:h2:mem:default`), recreada en cada inicio (`drop-and-create`)
- Datos iniciales cargados desde [import.sql](src/main/resources/import.sql)
- Las consultas JPA deben usar `@NamedQuery`, `@NamedNativeQuery` o la Criteria API — nunca `createQuery` con cadenas JPQL inline
- El dominio `user` contiene ejemplos explícitos de los tres enfoques, accesibles mediante los endpoints `/user/examples/*`

---

## Dependencias y Plugins

### Extensiones Quarkus

| Extensión | Función |
| --- | --- |
| [quarkus-rest](https://quarkus.io/guides/rest) + [quarkus-rest-jackson](https://quarkus.io/guides/rest-json) | Servidor REST reactivo con serialización Jackson |
| [quarkus-smallrye-openapi](https://quarkus.io/guides/openapi-swaggerui) | Generación automática del contrato OpenAPI y Swagger UI en `/q/swagger-ui` |
| [quarkus-hibernate-orm-panache](https://quarkus.io/guides/hibernate-orm-panache) | ORM con patrones Active Record y Repository simplificados |
| [quarkus-jdbc-h2](https://quarkus.io/guides/datasource) | Driver JDBC para la base de datos H2 en memoria |
| [quarkus-hibernate-validator](https://quarkus.io/guides/validation) | Bean Validation (`@NotBlank`, `@NotNull`, `@Valid`) en los DTOs de entrada |
| [quarkus-smallrye-health](https://quarkus.io/guides/smallrye-health) | Health checks en `/q/health`, `/q/health/live`, `/q/health/ready` |
| [quarkus-micrometer](https://quarkus.io/guides/micrometer) + [quarkus-micrometer-registry-prometheus](https://quarkus.io/guides/micrometer) | Métricas de la aplicación expuestas en `/q/metrics` (formato Prometheus) |
| [quarkus-info](https://quarkus.io/guides/info) | Metadatos de build y git en `/q/info` |
| [quarkus-opentelemetry](https://quarkus.io/guides/opentelemetry) | Distributed tracing via OTLP sin Java agent; correlaciona `traceId`/`spanId` en logs |

### Bibliotecas

| Biblioteca | Función |
| --- | --- |
| [MapStruct 1.6](https://mapstruct.org/) | Generación en tiempo de compilación de mappers entre entidades y DTOs |
| [Lombok 1.18](https://projectlombok.org/) | Reduce el boilerplate (`@RequiredArgsConstructor` para inyección por constructor) |
| [quarkus-resteasy-problem](https://github.com/quarkiverse/quarkus-resteasy-problem) | Errores HTTP en formato RFC 7807 (`application/problem+json`) |

### Plugins Maven

| Plugin | Función |
| --- | --- |
| [Spotless + Google Java Format](https://github.com/diffplug/spotless) | Formateo automático del código; falla el build si el código no está formateado |
| [quarkus-maven-plugin](https://quarkus.io/guides/maven-tooling) | Ciclo de vida Quarkus: `quarkus:dev`, `package`, build nativo |

### Pruebas

| Herramienta | Función |
| --- | --- |
| [quarkus-junit](https://quarkus.io/guides/getting-started-testing) | `@QuarkusTest` (pruebas unitarias/integración en JVM) y `@QuarkusIntegrationTest` (contra binario empaquetado) |
| [REST-assured](https://rest-assured.io/) | DSL fluente para probar endpoints HTTP |

---

## Cómo Ejecutar

```sh
# Modo dev (hot reload)
./run.sh

# Pruebas + validación de formato
./run-check.sh

# Corregir formato automáticamente
./run-spotless-apply.sh

# Build de producción
./run-build-prod.sh

# Build de imagen Docker y ejecución en contenedor
./run-docker.sh
```

Windows: use los equivalentes `.cmd`.

Con la aplicación en ejecución:

- Swagger UI: `http://localhost:8080/q/swagger-ui`
- Dev UI (consola H2): `http://localhost:8080/q/dev-ui`
- Health: `http://localhost:8080/q/health`
- Metrics: `http://localhost:8080/q/metrics`
- Info: `http://localhost:8080/q/info`

---

## Observabilidad

Cada solicitud recibe un `X-Request-Id` (preservado del cliente o generado automáticamente). Los logs incluyen `requestId`, `traceId` y `spanId`. Las métricas de negocio (`pet_create_total`, `user_create_total`) se exponen via Micrometer.

Para ejecutar el stack completo de observabilidad (OTEL Collector + Jaeger):

```sh
./mvnw package -DskipTests
docker-compose up
# Jaeger UI: http://localhost:16686
```
