# Quarkus MS Demo

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=alert_status)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=coverage)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=bugs)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=vulnerabilities)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=code_smells)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=reliability_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=security_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=sqale_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)

Também disponível em: [English](README.md) · [Español](README.es.md)

API REST educacional construída com **Java 21 + Quarkus 3.x**, inspirada no contrato Swagger Petstore. O objetivo é demonstrar boas práticas de arquitetura em camadas, persistência, observabilidade e testes em uma API REST moderna.

> Este projeto é educacional e não aborda segurança de produção (autenticação, autorização ou hardening).

## Licença

Licença MIT. Consulte o arquivo [LICENSE](LICENSE).

---

## Estrutura de Pacotes

O projeto segue uma organização por domínio com separação explícita de responsabilidades em camadas:

```text
src/main/java/org/acme/
├── pet/
│   ├── persistence/     # Entidade JPA + Repository
│   ├── resources/       # JAX-RS Resource (controller)
│   │   └── dtos/        # Records de entrada e saída (Request/Response)
│   └── services/        # Lógica de negócio
│       └── mappers/     # Conversão entidade ↔ DTO via MapStruct
├── store/               # Mesmo padrão do pet
├── user/                # Mesmo padrão do pet
└── shared/
    ├── ApiResponse.java         # Envelope genérico de resposta
    ├── ListResponse.java        # Envelope para listas simples
    ├── LoggingFilter.java       # Filtro JAX-RS: logs e X-Request-Id
    └── pagination/              # PagedResponse, PageMetadata, SortMetadata
```

Cada domínio (`pet`, `store`, `user`) é autocontido. A camada `shared` concentra os contratos de resposta reutilizáveis e os filtros transversais.

---

## Formato de Respostas para Listas

O projeto padroniza dois envelopes para retorno de coleções:

**Lista simples** — sem paginação:

```json
{
  "items": [ ... ]
}
```

**Lista paginada** — com metadados de página e ordenação:

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

Nunca retorne arrays puros no corpo da resposta. Sempre use `ListResponse<T>` ou `PagedResponse<T>` da camada `shared`.

---

## Persistência

- Banco H2 in-memory (`jdbc:h2:mem:default`), recriado a cada boot (`drop-and-create`)
- Dados iniciais carregados por [import.sql](src/main/resources/import.sql)
- Queries JPA obrigatoriamente via `@NamedQuery`, `@NamedNativeQuery` ou Criteria API — nunca `createQuery` com strings JPQL inline
- O domínio `user` contém exemplos explícitos das três abordagens, acessíveis via endpoints `/user/examples/*`

---

## Dependências e Plugins

### Quarkus Extensions

| Extensão | Função |
| --- | --- |
| [quarkus-rest](https://quarkus.io/guides/rest) + [quarkus-rest-jackson](https://quarkus.io/guides/rest-json) | Servidor REST reativo com serialização Jackson |
| [quarkus-smallrye-openapi](https://quarkus.io/guides/openapi-swaggerui) | Geração automática do contrato OpenAPI e Swagger UI em `/q/swagger-ui` |
| [quarkus-hibernate-orm-panache](https://quarkus.io/guides/hibernate-orm-panache) | ORM com Active Record e Repository patterns simplificados |
| [quarkus-jdbc-h2](https://quarkus.io/guides/datasource) | Driver JDBC para banco H2 in-memory |
| [quarkus-hibernate-validator](https://quarkus.io/guides/validation) | Bean Validation (`@NotBlank`, `@NotNull`, `@Valid`) nas requisições |
| [quarkus-smallrye-health](https://quarkus.io/guides/smallrye-health) | Health checks em `/q/health`, `/q/health/live`, `/q/health/ready` |
| [quarkus-micrometer](https://quarkus.io/guides/micrometer) + [quarkus-micrometer-registry-prometheus](https://quarkus.io/guides/micrometer) | Métricas de aplicação expostas em `/q/metrics` (formato Prometheus) |
| [quarkus-info](https://quarkus.io/guides/info) | Metadados de build e git em `/q/info` |
| [quarkus-opentelemetry](https://quarkus.io/guides/opentelemetry) | Distributed tracing via OTLP sem Java agent; correlaciona `traceId`/`spanId` nos logs |

### Bibliotecas

| Biblioteca | Função |
| --- | --- |
| [MapStruct 1.6](https://mapstruct.org/) | Geração em tempo de compilação de mappers entre entidades e DTOs |
| [Lombok 1.18](https://projectlombok.org/) | Reduz boilerplate (`@RequiredArgsConstructor` para injeção via construtor) |
| [quarkus-resteasy-problem](https://github.com/quarkiverse/quarkus-resteasy-problem) | Erros HTTP no formato RFC 7807 (`application/problem+json`) |

### Plugins Maven

| Plugin | Função |
| --- | --- |
| [Spotless + Google Java Format](https://github.com/diffplug/spotless) | Formatação automática do código; falha o build se o código não estiver formatado |
| [quarkus-maven-plugin](https://quarkus.io/guides/maven-tooling) | Ciclo de vida Quarkus: `quarkus:dev`, `package`, build nativo |

### Testes

| Ferramenta | Função |
| --- | --- |
| [quarkus-junit](https://quarkus.io/guides/getting-started-testing) | `@QuarkusTest` (testes unitários/integração em JVM) e `@QuarkusIntegrationTest` (contra binário empacotado) |
| [REST-assured](https://rest-assured.io/) | DSL fluente para testar endpoints HTTP |

---

## Como Executar

```sh
# Dev mode (hot reload)
./run.sh

# Testes + validação de formatação
./run-check.sh

# Corrigir formatação automaticamente
./run-spotless-apply.sh

# Build de produção
./run-build-prod.sh

# Build da imagem Docker e execução em container
./run-docker.sh
```

Windows: use os equivalentes `.cmd`.

Com a aplicação rodando:

- Swagger UI: `http://localhost:8080/q/swagger-ui`
- Dev UI (H2 console): `http://localhost:8080/q/dev-ui`
- Health: `http://localhost:8080/q/health`
- Metrics: `http://localhost:8080/q/metrics`
- Info: `http://localhost:8080/q/info`

---

## Observabilidade

Cada requisição recebe um `X-Request-Id` (preservado do cliente ou gerado automaticamente). Logs incluem `requestId`, `traceId`, `spanId`. Métricas de negócio (`pet_create_total`, `user_create_total`) são expostas via Micrometer.

Para rodar o stack completo de observabilidade (OTEL Collector + Jaeger):

```sh
./mvnw package -DskipTests
docker-compose up
# Jaeger UI: http://localhost:16686
```
