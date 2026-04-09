# Quarkus MS Demo

API REST em Quarkus inspirada no contrato do Swagger Petstore, organizada em camadas simples para facilitar estudo, evolução e testes.

O projeto expõe três áreas principais:

- `pet`: cadastro e consulta de pets em memória
- `store`: inventário e pedidos em memória
- `user`: operações de usuário com persistência em H2 usando Panache

## Stack

- Java 17
- Quarkus 3.34.3
- REST com Jackson
- SmallRye OpenAPI + Swagger UI
- Hibernate ORM Panache
- H2 em memória
- JUnit + RestAssured
- Spotless com Google Java Format

## Como rodar

Pré-requisitos:

- JDK 17+

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

## Scripts úteis

- `./run.sh`: inicia a aplicação em modo dev
- `./run-check.sh`: roda testes e valida formatação
- `./run-spotless-apply.sh`: corrige formatação Java automaticamente

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
- `POST /user`
- `POST /user/createWithArray`
- `POST /user/createWithList`
- `GET /user/login`
- `GET /user/logout`
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

Os domínios `pet` e `store` usam repositórios em memória com dados seedados no próprio código.

## Estrutura do projeto

```text
src/main/java/org/acme
├── pet
│   ├── dtos
│   ├── persistence
│   ├── resources
│   └── services
├── store
├── user
└── shared
```

Resumo por pacote:

- `pet`: separação explícita entre recurso HTTP, serviço, DTOs e repositório em memória
- `store`: fluxo de pedidos e inventário
- `user`: entidade JPA, repositório Panache, serviço e recurso REST
- `shared`: filtros e componentes transversais, como logging de requests

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
- Há arquivos estáticos legados em `src/main/resources/META-INF/resources`, mas o foco atual do projeto está na API REST.
- O projeto recomenda a extensão VS Code `redhat.vscode-quarkus` em `.vscode/extensions.json`.
