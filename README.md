# Quarkus MS Demo

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=alert_status)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=coverage)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=bugs)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=vulnerabilities)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=code_smells)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=reliability_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=security_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=tiagolpadua_quarkus-ms-demo&metric=sqale_rating)](https://sonarcloud.io/project/overview?id=tiagolpadua_quarkus-ms-demo)

API REST em Quarkus inspirada no contrato do Swagger Petstore, organizada em camadas simples para facilitar estudo, evolução e testes.

## Licença

Este projeto está licenciado sob a licença MIT. Veja o arquivo [LICENSE](LICENSE).

## Escopo didático

- Este projeto é didático e não entra no mérito de segurança de produção (autenticação, autorização e hardening).
- O projeto é agnóstico em relação a banco; o H2 em memória é apenas a configuração padrão para execução local e testes rápidos.

O projeto expõe três áreas principais:

- `pet`: cadastro e consulta de pets persistidos em H2
- `store`: inventário e pedidos persistidos em H2
- `user`: operações de usuário com persistência em H2 usando Panache

## Stack

- Java 21
- Quarkus 3.34.3
- REST com Jackson
- SmallRye OpenAPI + Swagger UI
- Hibernate ORM Panache
- H2 em memória
- SmallRye Health
- Micrometer
- Quarkus Info
- OpenTelemetry (OTLP tracing)
- RESTEasy Problem (RFC 7807)
- JUnit + RestAssured
- Spotless com Google Java Format

## Como rodar

Pré-requisitos:

- JDK 21+

Suba a aplicação em modo de desenvolvimento:

```sh
./run.sh
```

Ou, se preferir, diretamente com Maven Wrapper:

```sh
./mvnw quarkus:dev
```

Com a aplicação em execução:

- API: `http://localhost:8080`
- OpenAPI: `http://localhost:8080/q/openapi`
- Swagger UI: `http://localhost:8080/q/swagger-ui`
- Dev UI: `http://localhost:8080/q/dev-ui`
- Health: `http://localhost:8080/q/health`
- Readiness: `http://localhost:8080/q/health/ready`
- Liveness: `http://localhost:8080/q/health/live`
- Metrics: `http://localhost:8080/q/metrics`
- Info: `http://localhost:8080/q/info`

Para inspecionar o banco H2 em memória durante o desenvolvimento, use:

- `http://localhost:8080/q/dev-ui`

Na Dev UI, abra a seção de datasource/H2 para navegar pelas tabelas e executar consultas SQL. O projeto já está configurado com `%dev.quarkus.datasource.dev-ui.allow-sql=true` para permitir esse uso em ambiente de desenvolvimento.

## Observabilidade

O projeto possui suporte nativo a OpenTelemetry via extensão oficial do Quarkus, com tracing OTLP e correlação de `traceId` e `spanId` nos logs.

Também foram incluídas extensões complementares de operação:

- `quarkus-smallrye-health`: health checks padrão do Quarkus
- `quarkus-micrometer`: métricas da aplicação, JVM, sistema e servidor HTTP
- `quarkus-info`: metadados de build e git em endpoint dedicado

Configurações principais:

- `OTEL_EXPORTER_OTLP_ENDPOINT`: endpoint OTLP do collector. Padrão: `http://localhost:4317`
- `OTEL_EXPORTER_OTLP_PROTOCOL`: protocolo OTLP. Padrão: `grpc`
- `OTEL_EXPORTER_OTLP_HEADERS`: headers extras para autenticação com o collector
- `OTEL_TRACES_SAMPLER`: sampler OTel. Padrão: `parentbased_traceidratio`
- `OTEL_TRACES_SAMPLER_ARG`: taxa de amostragem quando aplicável. Padrão: `1.0`
- `DEPLOYMENT_ENVIRONMENT`: atributo `deployment.environment` enviado nos recursos OTel. Padrão: `dev`
- `OTEL_EXPORTER_OTLP_ENABLED`: em `dev`, controla se a exportação OTLP fica ativa. Padrão: `false`

Boas práticas adotadas:

- sem uso de Java agent, aproveitando a instrumentação nativa do Quarkus
- tracing habilitado com export OTLP configurável por ambiente
- logs com correlação de trace para facilitar troubleshooting
- SDK OTel desabilitado em testes para manter a suíte estável e silenciosa
- métricas seguem Micrometer, que é o caminho recomendado pelo Quarkus para esse sinal

Como usar:

- `GET /q/health`: visão agregada de saúde da aplicação
- `GET /q/health/live`: liveness probe
- `GET /q/health/ready`: readiness probe
- `GET /q/metrics`: métricas da aplicação e infraestrutura
- `GET /q/info`: informações de build e git quando disponíveis

## Scripts úteis

- `./run.sh`: inicia a aplicação em modo dev
- `./run-check.sh`: roda testes e valida formatação
- `./run-spotless-apply.sh`: corrige formatação Java automaticamente
- `./run-build-prod.sh`: gera o build de produção com `clean verify`
- `./run-docker.sh`: gera a imagem Docker JVM e sobe a aplicação em container

Equivalentes para Windows:

- `run.cmd`
- `run-check.cmd`
- `run-spotless-apply.cmd`
- `run-build-prod.cmd`
- `run-docker.cmd`

Build de produção:

```sh
./run-build-prod.sh
```

Execução em Docker:

```sh
./run-docker.sh
```

Variáveis opcionais para o script Docker:

- `IMAGE_NAME`: nome da imagem. Padrão: `quarkus-ms-demo:jvm`
- `CONTAINER_NAME`: nome do container. Padrão: `quarkus-ms-demo`
- `HOST_PORT`: porta publicada no host. Padrão: `8080`

## Endpoints principais

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

## Persistência e dados iniciais

O projeto usa H2 em memória:

- datasource: `jdbc:h2:mem:default`
- estratégia de schema: `drop-and-create`

Ao subir a aplicação, usuários iniciais são carregados por [import.sql](/Volumes/LEXAR_1TB/git/quarkus-ms-demo/src/main/resources/import.sql):

- `seed-user-1`
- `seed-user-2`

Os domínios `pet`, `store` e `user` usam H2 em memória com schema recriado a cada boot e dados seedados por [import.sql](/Volumes/LEXAR_1TB/git/quarkus-ms-demo/src/main/resources/import.sql).

## Estrutura do projeto

```text
src/main/java/org/acme
├── pet
│   ├── dtos
│   ├── mappers
│   ├── persistence
│   ├── resources
│   └── services
├── store
│   ├── dtos
│   ├── mappers
│   ├── persistence
│   ├── resources
│   └── services
├── user
│   ├── dtos
│   ├── mappers
│   ├── persistence
│   ├── resources
│   └── services
└── shared
```

Resumo por pacote:

- `pet`: separação explícita entre recurso HTTP, serviço, DTOs e persistência JPA
- `store`: segue a mesma organização de `pet`, com DTOs, mappers, persistência, resources e services
- `user`: segue a mesma organização de `pet`, com DTOs, mappers, persistência, resources e services
- `shared`: filtros e componentes transversais, como logging de requests

## Exemplos de acesso ao banco

Além do uso de Panache, o projeto também contém exemplos explícitos de outras abordagens JPA no domínio `user`:

- `NamedQuery`: `GET /user/examples/named-query?status=1`
- `NamedNativeQuery`: `GET /user/examples/named-native-query?emailDomain=example.com`
- `Criteria API`: `GET /user/examples/criteria?usernamePrefix=seed-user&status=1&emailDomain=example.com`

Esses exemplos estão implementados em [User.java](/Volumes/LEXAR_1TB/git/quarkus-ms-demo/src/main/java/org/acme/user/persistence/User.java), [UserRepository.java](/Volumes/LEXAR_1TB/git/quarkus-ms-demo/src/main/java/org/acme/user/persistence/UserRepository.java) e [UserResource.java](/Volumes/LEXAR_1TB/git/quarkus-ms-demo/src/main/java/org/acme/user/resources/UserResource.java).

## Qualidade e testes

Para rodar a suíte local:

```sh
./mvnw test
```

Os testes cobrem:

- endpoints `pet`
- endpoints `store`
- endpoints `user`
- documento OpenAPI e Swagger UI

## Observações

- A documentação OpenAPI publicada pela aplicação usa o título `Swagger Petstore`.
- Erros HTTP produzidos pelos resources seguem o formato `application/problem+json` via extensão `quarkus-resteasy-problem`.
- Há arquivos estáticos legados em `src/main/resources/META-INF/resources`, mas o foco atual do projeto está na API REST.
- O projeto recomenda a extensão VS Code `redhat.vscode-quarkus` em `.vscode/extensions.json`.
