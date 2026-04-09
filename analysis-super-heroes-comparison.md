# Análise Comparativa: quarkus-ms-demo vs quarkus-super-heroes

> **Objetivo:** Identificar bibliotecas, padrões e boas práticas presentes no projeto
> [quarkus-super-heroes](https://github.com/quarkusio/quarkus-super-heroes) que ainda não foram
> adotados no `quarkus-ms-demo`. Esta análise cobre todos os módulos do projeto de referência:
> `rest-heroes`, `rest-villains`, `rest-fights`, `rest-narration`, `event-statistics`, e
> `grpc-locations`.
>
> **Importante:** Nenhuma alteração foi feita no projeto atual. Este documento é apenas informativo.

---

## Sumário

- [1. Contexto e Escopo](#1-contexto-e-escopo)
- [2. Testes e Qualidade](#2-testes-e-qualidade)
- [3. Padrões de Código e Boas Práticas](#3-padrões-de-código-e-boas-práticas)
- [4. Observabilidade Avançada](#4-observabilidade-avançada)
- [5. Configuração](#5-configuração)
- [6. Resiliência e Tolerância a Falhas](#6-resiliência-e-tolerância-a-falhas)
- [7. Infraestrutura e Deploy](#7-infraestrutura-e-deploy)
- [8. Tecnologias Adicionais (escopo ampliado)](#8-tecnologias-adicionais-escopo-ampliado)
- [9. Tabela Resumo](#9-tabela-resumo)
- [10. Recomendações Priorizadas](#10-recomendações-priorizadas)

---

## 1. Contexto e Escopo

### quarkus-ms-demo

| Característica | Valor                                          |
| -------------- | ---------------------------------------------- |
| Tipo           | Aplicação Quarkus única (monolito de domínios) |
| Linguagem      | Java 21                                        |
| Versão Quarkus | 3.34.3                                         |
| Banco de dados | H2 in-memory                                   |
| Persistência   | Hibernate ORM Panache (bloqueante)             |
| Testes         | `quarkus-junit` + REST-Assured                 |
| Cobertura      | `jacoco-maven-plugin` standalone               |
| Formatação     | Spotless + Google Java Format                  |

### quarkus-super-heroes

| Módulo             | Descripção                     | Linguagem | Stack de Persistência                                |
| ------------------ | ------------------------------ | --------- | ---------------------------------------------------- |
| `rest-heroes`      | CRUD de heróis                 | Java      | Hibernate Reactive Panache + PostgreSQL              |
| `rest-villains`    | CRUD de vilões                 | Java      | Hibernate ORM Panache + PostgreSQL (Virtual Threads) |
| `rest-fights`      | Batalhas entre heróis e vilões | Java      | MongoDB Panache + Kafka + gRPC client                |
| `rest-narration`   | Narração com AI                | Java      | Nenhuma (LangChain4j + OpenAI/Azure)                 |
| `event-statistics` | Estatísticas via eventos       | Java      | Nenhuma (Kafka + WebSockets)                         |
| `grpc-locations`   | Serviço gRPC de localizações   | Kotlin    | Hibernate ORM Panache Kotlin + MariaDB               |

---

## 2. Testes e Qualidade

Esta é a área com maior delta entre os dois projetos. O `quarkus-super-heroes` adota uma
pirâmide de testes mais completa.

### 2.3 quarkus-panache-mock — Ausente no ms-demo

**Módulos com uso:** `rest-villains`, `rest-fights`.

Fornece utilitários para mockar métodos estáticos de entidades Panache (ex.: `Villain.findRandom()`)
em testes sem precisar de banco de dados real.

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-panache-mock</artifactId>
  <scope>test</scope>
</dependency>
```

---

### 2.4 maven-surefire-junit5-tree-reporter — Ausente no ms-demo

**Módulos com uso:** todos os módulos do super-heroes.

Plugin que melhora a saída do `mvn test` formatando os resultados dos testes em estrutura de
árvore no console, facilitando a identificação de falhas.

```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <dependencies>
    <dependency>
      <groupId>me.fabriciorby</groupId>
      <artifactId>maven-surefire-junit5-tree-reporter</artifactId>
      <version>1.5.1</version>
    </dependency>
  </dependencies>
  <configuration>
    <reportFormat>plain</reportFormat>
    <statelessTestsetInfoReporter
      implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
      <printStacktraceOnError>true</printStacktraceOnError>
      <printStacktraceOnFailure>true</printStacktraceOnFailure>
      <printStdoutOnSuccess>false</printStdoutOnSuccess>
    </statelessTestsetInfoReporter>
  </configuration>
</plugin>
```

---

### 2.6 quarkus-jacoco vs jacoco-maven-plugin — Diferença de abordagem

**Módulos com uso:** todos os módulos do super-heroes usam `quarkus-jacoco` (test scope).

O ms-demo usa o `jacoco-maven-plugin` standalone (adicionado recentemente). O super-heroes
usa a extensão `quarkus-jacoco`, que integra nativamente com Quarkus e funciona corretamente
com o classloading customizado do Quarkus, evitando problemas de instrumentação.

Além disso, o super-heroes separa cobertura de IT num perfil dedicado (`it-coverage`):

```xml
<!-- quarkus-jacoco (test scope) — mais integrado ao Quarkus -->
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-jacoco</artifactId>
  <scope>test</scope>
</dependency>

<!-- Perfil it-coverage para cobertura de integração separada -->
<profile>
  <id>it-coverage</id>
  <build>
    <plugins>
      <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <executions>
          <execution>
            <goals><goal>prepare-agent-integration</goal></goals>
            <configuration>
              <destFile>${project.build.directory}/jacoco-quarkus.exec</destFile>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</profile>
```

---

### 2.7 Testes Parametrizados — Ausente no ms-demo

**Módulos com uso:** `rest-villains` (VillainTests).

O super-heroes usa `@ParameterizedTest` com `@ValueSource`, `@NullSource` e `@EmptySource`
para cobrir múltiplos cenários de entrada com um único método de teste.

```java
@ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER + "[" + INDEX_PLACEHOLDER + "]")
@ValueSource(strings = { DEFAULT_NAME, "choco", "Choco", "super" })
@EmptySource
public void findAllWhereNameLikeFound(String name) {
  assertThat(Villain.listAllWhereNameLike(name)).hasSize(1);
}

@ParameterizedTest
@ValueSource(strings = { "nenhum", "inexistente" })
@NullSource
public void findAllWhereNameLikeNotFound(String name) {
  assertThat(Villain.listAllWhereNameLike(name)).isEmpty();
}
```

---

### 2.8 @TestTransaction — Ausente no ms-demo

**Módulos com uso:** `rest-villains` (VillainTests).

A anotação `@TestTransaction` faz rollback automático da transação após cada teste, garantindo
isolamento sem precisar limpar dados manualmente no `@AfterEach`.

```java
@QuarkusTest
@TestTransaction
class VillainTests {
  @Test
  void findRandomFound() {
    Villain.deleteAll();
    Villain.persist(villain);
    assertThat(Villain.findRandom()).isPresent();
    // rollback automático após o teste
  }
}
```

---

### 2.9 Consumer-Driven Contract Testing com Pact — Ausente no ms-demo

**Módulos com uso:** `rest-heroes`, `rest-villains`, `rest-fights`, `rest-narration`,
`grpc-locations`.

Garante que o contrato de API entre produtores e consumidores não seja quebrado acidentalmente.
Cada módulo possui um `ContractVerificationTests.java` usando `@Provider` e `quarkus-pact-provider`.

```xml
<dependency>
  <groupId>io.quarkiverse.pact</groupId>
  <artifactId>quarkus-pact-provider</artifactId>
  <version>1.6.0</version>
  <scope>test</scope>
</dependency>
```

```java
@Provider("rest-villains")
@QuarkusTest
public class ContractVerificationTests {
  // verifica contratos consumidos por rest-fights e outros
}
```

---

### 2.10 Playwright para Testes de UI — Ausente no ms-demo

**Módulos com uso:** `rest-heroes` (UIResourceTests), `rest-villains` (UIResourceTests).

O super-heroes testa a UI de Qute templates via Playwright, validando o comportamento do
navegador de forma programática.

```xml
<dependency>
  <groupId>io.quarkiverse.playwright</groupId>
  <artifactId>quarkus-playwright</artifactId>
  <version>2.3.3</version>
  <scope>test</scope>
</dependency>
```

---

### 2.11 WireMock para Virtualização de Serviços Externos — Ausente no ms-demo

**Módulos com uso:** `rest-narration` (quarkus-wiremock), `rest-fights` (wiremock + wiremock-grpc).

Permite simular APIs externas em testes sem precisar de infrastructure real.

```xml
<!-- rest-narration -->
<dependency>
  <groupId>io.quarkiverse.wiremock</groupId>
  <artifactId>quarkus-wiremock-test</artifactId>
  <version>1.6.1</version>
  <scope>test</scope>
</dependency>
```

---

### 2.12 @TestProfile e QuarkusTestProfile — Ausente no ms-demo

**Módulos com uso:** `rest-narration`, `rest-fights`.

Permite criar perfis de configuração específicos para conjuntos de testes, sobrescrevendo
propriedades ou desabilitando extensões seletivamente.

```java
@QuarkusTest
@TestProfile(DisabledTestProfile.class)
class NarrationServiceDisabledTests {
  public static class DisabledTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("quarkus.langchain4j.openai.enabled", "false");
    }
  }
}
```

---

## 3. Padrões de Código e Boas Práticas

### 3.1 @RunOnVirtualThread (Project Loom) — Ausente no ms-demo

**Módulos com uso:** `rest-villains` (em todos os métodos de `VillainResource`).

O `rest-villains` usa Virtual Threads do Java 21 para execução dos endpoints, demonstrando
como migrar um serviço bloqueante para o modelo de Virtual Threads sem reescrever para
programação reativa.

```java
// VillainResource.java
@GET
@Path("/random")
@RunOnVirtualThread   // <-- executa em virtual thread
public Response getRandomVillain() {
  return this.service.findRandomVillain()
    .map(v -> Response.ok(v).build())
    .orElseGet(() -> Response.status(NOT_FOUND).build());
}
```

Para validar a ausência de pinning nos testes, o super-heroes usa:

```java
@QuarkusTest
@VirtualThreadUnit  // ativa o executor de virtual threads nos testes
@ShouldNotPin       // falha o teste se ocorrer pinning da carrier thread
class VillainResourceTests { ... }
```

---

### 3.2 MicroProfile OpenAPI — Annotations Ricas — Ausente no ms-demo

**Módulos com uso:** `rest-heroes`, `rest-villains`.

O ms-demo gera OpenAPI via configuração em `application.properties` mas não usa anotações
MicroProfile OpenAPI diretamente no código. O super-heroes documenta cada endpoint com
`@Operation`, `@APIResponse`, `@Schema`, `@ExampleObject`, `@Parameter`, `@RequestBody` e `@Tag`.

```java
// HeroResource.java
@GET
@Path("/random")
@Operation(summary = "Returns a random hero")
@APIResponse(
  responseCode = "200",
  description = "Gets a random hero",
  content = @Content(
    mediaType = APPLICATION_JSON,
    schema = @Schema(implementation = Hero.class, required = true),
    examples = @ExampleObject(name = "hero", value = Examples.VALID_EXAMPLE_HERO)
  )
)
@APIResponse(responseCode = "404", description = "No hero found")
public Uni<Response> getRandomHero() { ... }
```

---

### 3.3 @ConfigMapping — Ausente no ms-demo

**Módulos com uso:** `rest-villains` (VillainConfig), `rest-fights` (FightConfig).

O super-heroes encapsula propriedades customizadas em interfaces tipadas usando `@ConfigMapping`
do SmallRye Config, em vez de injetar `@ConfigProperty` individualmente nos beans.

```java
// rest-villains/config/VillainConfig.java
@ConfigMapping(prefix = "villain")
public interface VillainConfig {
  Level level();

  interface Level {
    @WithDefault("1.0")
    double multiplier();
  }
}
```

```properties
# application.properties
villain.level.multiplier=0.5
%test.villain.level.multiplier=1
```

---

### 3.4 Lifecycle Events — Ausente no ms-demo

**Módulos com uso:** `rest-villains` (VillainApplicationLifeCycle).

O super-heroes demonstra o uso de `StartupEvent` e `ShutdownEvent` para logar informações
de startup da aplicação, incluindo os perfis ativos.

```java
@ApplicationScoped
public class VillainApplicationLifeCycle {
  void onStart(@Observes StartupEvent ev) {
    Log.info("The application VILLAIN is starting with profile "
      + ConfigUtils.getProfiles());
  }

  void onStop(@Observes ShutdownEvent ev) {
    Log.info("The application VILLAIN is stopping...");
  }
}
```

---

### 3.5 Custom HealthCheck de Liveness — Ausente no ms-demo

**Módulos com uso:** `rest-heroes` (PingHeroResourceHealthCheck), `rest-villains`
(PingVillainResourceHealthCheck).

Além das health checks padrão do Quarkus, o super-heroes implementa checks customizados
que pingam o próprio endpoint `/hello` como prova de que a camada HTTP está funcional.

```java
@Liveness
public class PingHeroResourceHealthCheck implements HealthCheck {
  private final HeroResource heroResource;

  @Override
  public HealthCheckResponse call() {
    var response = this.heroResource.hello();
    return HealthCheckResponse.named("Ping Hero REST Endpoint")
      .withData("Response", response)
      .up()
      .build();
  }
}
```

---

### 3.6 Application Banner Customizado — Ausente no ms-demo

**Módulos com uso:** `rest-heroes`, `rest-villains`.

O super-heroes define um `banner.txt` customizado em `src/main/resources/` exibido no startup.

```properties
quarkus.banner.path=banner.txt
```

---

### 3.7 Qute Templates para UI — Ausente no ms-demo

**Módulos com uso:** `rest-heroes`, `rest-villains`.

Um endpoint `/` serve uma página HTML gerada via Qute (motor de templates do Quarkus),
exibindo dados da API. Isso demonstra renderização server-side sem framework externo.

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-rest-qute</artifactId>
</dependency>
```

---

### 3.8 Clients REST Tipados (@RegisterRestClient) — Ausente no ms-demo

**Módulos com uso:** `rest-fights`.

O `rest-fights` consome `rest-heroes`, `rest-villains` e `rest-narration` via MicroProfile
REST Client tipado, com configuração externalizada.

```java
@Path("/api/heroes")
@Produces(APPLICATION_JSON)
@RegisterRestClient(configKey = "hero-client")
@RegisterClientHeaders  // propagação automática de headers (ex: correlationId)
interface HeroRestClient {
  @GET
  @Path("/random")
  Uni<Hero> findRandomHero();
}
```

---

### 3.9 quarkus.jackson.serialization-inclusion=non-empty — Ausente no ms-demo

**Módulos com uso:** `rest-heroes`, `rest-villains`.

Omite campos `null` e coleções vazias das respostas JSON por padrão, reduzindo o tamanho
dos payloads sem precisar de anotações `@JsonInclude` em cada DTO.

```properties
quarkus.jackson.serialization-inclusion=non-empty
quarkus.rest.jackson.optimization.enable-reflection-free-serializers=true
```

---

## 4. Observabilidade Avançada

### 4.2 opentelemetry-jdbc — Ausente no ms-demo

**Módulos com uso:** `rest-villains`, `grpc-locations`.

Instrumentação automática de chamadas JDBC pelo OpenTelemetry, permitindo ver queries SQL
como spans filhos nos traces.

```xml
<dependency>
  <groupId>io.opentelemetry.instrumentation</groupId>
  <artifactId>opentelemetry-jdbc</artifactId>
</dependency>
```

```properties
quarkus.datasource.jdbc.telemetry=true
```

---

### 4.3 Configuração de Log por Console com Darken — Ausente no ms-demo

**Módulos com uso:** `rest-heroes`, `rest-villains`.

```properties
quarkus.log.console.darken=1  # reduz brilho de output normal para destacar warnings/erros
```

---

## 5. Configuração

### 5.1 YAML Configuration (quarkus-config-yaml) — Ausente no ms-demo

**Módulos com uso:** `rest-heroes`, `grpc-locations`.

O `rest-heroes` usa `application.yml` em vez de `application.properties`, com suporte a
perfis via YAML multi-documento.

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-config-yaml</artifactId>
</dependency>
```

```yaml
"%dev,test":
  quarkus:
    log:
      console:
        level: DEBUG

"%prod":
  quarkus:
    hibernate-orm:
      sql-load-script: import.sql
```

---

### 5.2 Perfis Maven de Build — Ausente no ms-demo

**Módulos com uso:** todos os módulos do super-heroes.

O super-heroes define perfis Maven para cenários específicos:

```xml
<!-- Perfil para build nativo -->
<profile>
  <id>native</id>
  <activation>
    <property><name>native</name></property>
  </activation>
  <properties>
    <quarkus.native.enabled>true</quarkus.native.enabled>
    <quarkus.package.jar.enabled>false</quarkus.package.jar.enabled>
  </properties>
</profile>

<!-- Perfil para cobertura de integration tests -->
<profile>
  <id>it-coverage</id>
  ...
</profile>
```

---

### 5.3 CORS Habilitado — Ausente no ms-demo

**Módulos com uso:** `rest-heroes`, `rest-villains`.

Com a UI separada (`ui-super-heroes`), o super-heroes habilita CORS para permitir que o
frontend consuma as APIs.

```properties
quarkus.http.cors.enabled=true
quarkus.http.cors.origins=*
```

---

### 5.4 Configurações de Profile para Produção — Ausente no ms-demo

**Módulos com uso:** todos os módulos do super-heroes.

O super-heroes usa `%prod.quarkus.hibernate-orm.sql-load-script=import.sql` para carregar
dados apenas em produção, enquanto no ms-demo `import.sql` é carregado em todos os perfis
via `drop-and-create`.

---

## 6. Resiliência e Tolerância a Falhas

### 6.1 SmallRye Fault Tolerance — Ausente no ms-demo

**Módulos com uso:** `rest-fights` (HeroClient, VillainClient, LocationClient),
`rest-narration` (NarrationService).

O super-heroes usa MicroProfile Fault Tolerance com anotações declarativas para resilência
contra falhas em chamadas externas.

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-smallrye-fault-tolerance</artifactId>
</dependency>
```

```java
// HeroClient.java
@CircuitBreaker(requestVolumeThreshold = 8, failureRatio = 0.5,
                delay = 2, delayUnit = ChronoUnit.SECONDS)
@CircuitBreakerName("findRandomHero")
@Retry(maxRetries = 3, delay = 200, delayUnit = ChronoUnit.MILLIS)
@Fallback(fallbackMethod = "fallbackRandomHero")
public Uni<Hero> findRandomHero() { ... }
```

> **Relevância para ms-demo:** Embora o ms-demo não consuma outros serviços hoje, este padrão
> é fundamental para qualquer cenário futuro de integração com sistemas externos.

---

## 7. Infraestrutura e Deploy

### 7.1 Container Image Build (quarkus-container-image-docker) — Ausente no ms-demo

**Módulos com uso:** todos os módulos do super-heroes (exceto `grpc-locations` usa diferente).

O super-heroes delega o build e push da imagem Docker ao Quarkus com configuração unificada.

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-container-image-docker</artifactId>
</dependency>
```

```properties
quarkus.container-image.builder=docker
quarkus.container-image.registry=quay.io
quarkus.container-image.group=quarkus-super-heroes
quarkus.container-image.name=${quarkus.application.name}
```

---

### 7.2 Kubernetes / OpenShift / Minikube Manifests — Ausente no ms-demo

**Módulos com uso:** todos os módulos do super-heroes.

O super-heroes gera automaticamente manifests Kubernetes, OpenShift, Minikube e Knative
através das extensões Quarkus correspondentes.

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-kubernetes</artifactId>
</dependency>
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-openshift</artifactId>
</dependency>
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-minikube</artifactId>
</dependency>
```

---

### 7.3 Banco de Dados de Produção — Ausente no ms-demo

O ms-demo usa exclusivamente H2 in-memory. O super-heroes usa bancos adequados para produção:

| Módulo           | Banco      | Extensão Quarkus             |
| ---------------- | ---------- | ---------------------------- |
| `rest-heroes`    | PostgreSQL | `quarkus-reactive-pg-client` |
| `rest-villains`  | PostgreSQL | `quarkus-jdbc-postgresql`    |
| `rest-fights`    | MongoDB    | `quarkus-mongodb-panache`    |
| `grpc-locations` | MariaDB    | `quarkus-jdbc-mariadb`       |

O uso do H2 é adequado para aprendizado e demo, mas um perfil de banco externo (PostgreSQL
via Quarkus Dev Services) seria benéfico para testes de integração mais realistas.

---

### 7.4 Liquibase para Migrações de Schema — Ausente no ms-demo

**Módulos com uso:** `rest-fights` (quarkus-liquibase-mongodb).

O ms-demo usa `drop-and-create` com `import.sql`. O super-heroes (para MongoDB) usa Liquibase.
Para bancos relacionais em produção, Flyway ou Liquibase são a abordagem recomendada.

---

## 8. Tecnologias Adicionais (escopo ampliado)

Estas tecnologias fazem parte do escopo do super-heroes como showcase, mas são arquiteturalmente
incompatíveis com o objetivo de aplicação única do ms-demo. Listadas aqui para referência.

| Tecnologia                                       | Módulo                            | Descrição                                          |
| ------------------------------------------------ | --------------------------------- | -------------------------------------------------- |
| **gRPC** (`quarkus-grpc`)                        | `grpc-locations`, `rest-fights`   | Servidor e cliente gRPC com Protocol Buffers       |
| **Kafka** (`quarkus-messaging-kafka`)            | `rest-fights`, `event-statistics` | Mensageria assíncrona com Avro + Apicurio Registry |
| **WebSockets** (`quarkus-websockets-next`)       | `event-statistics`                | Push de estatísticas em tempo real                 |
| **Hibernate Reactive**                           | `rest-heroes`                     | Acesso reativo ao banco com `Uni<>` / `Multi<>`    |
| **LangChain4j + OpenAI** (`quarkus-langchain4j`) | `rest-narration`                  | AI generativa com `@RegisterAiService`             |
| **SmallRye Stork**                               | `rest-fights`                     | Service discovery e load balancing                 |
| **Kotlin**                                       | `grpc-locations`                  | Suporte a Kotlin no ecossistema Quarkus            |
| **quarkus-arc** explícito                        | vários módulos                    | Declaração explícita do container CDI              |

---

## 9. Tabela Resumo

| Item                                                | Categoria       | Presente em ms-demo | Presente em super-heroes                   | Prioridade Recomendada |
| --------------------------------------------------- | --------------- | ------------------- | ------------------------------------------ | ---------------------- |
| `@ParameterizedTest`                                | Testes          | Não                 | rest-villains                              | Média                  |
| `opentelemetry-jdbc`                                | Observabilidade | Não                 | rest-villains, grpc-locations              | Média                  |
| `@ConfigMapping` tipado                             | Configuração    | Não                 | rest-villains, rest-fights                 | Média                  |
| `quarkus-panache-mock`                              | Testes          | Não                 | rest-villains, rest-fights                 | Média                  |
| `maven-surefire-junit5-tree-reporter`               | Build           | Não                 | Todos os módulos                           | Baixa                  |
| `quarkus-jacoco` extension vs plugin standalone     | Cobertura       | Plugin standalone   | Extension (test scope)                     | Baixa                  |
| `quarkus-smallrye-fault-tolerance`                  | Resiliência     | Não                 | rest-fights, rest-narration                | Baixa\*                |
| `@RunOnVirtualThread`                               | Runtime         | Não                 | rest-villains                              | Média                  |
| Custom `HealthCheck` (`@Liveness`)                  | Observabilidade | Não                 | rest-heroes, rest-villains                 | Baixa                  |
| `@ConfigMapping`                                    | Configuração    | Não                 | rest-villains, rest-fights                 | Média                  |
| Lifecycle events (`StartupEvent/ShutdownEvent`)     | Padrão          | Não                 | rest-villains                              | Baixa                  |
| MicroProfile OpenAPI annotations completas          | Documentação    | Não                 | rest-heroes, rest-villains                 | Média                  |
| `quarkus.jackson.serialization-inclusion=non-empty` | Configuração    | Não                 | rest-heroes, rest-villains                 | Baixa                  |
| CORS habilitado                                     | Configuração    | Não                 | rest-heroes, rest-villains                 | Baixa\*                |
| `quarkus-config-yaml`                               | Configuração    | Não                 | rest-heroes, grpc-locations                | Baixa                  |
| `banner.txt` customizado                            | DX              | Não                 | rest-heroes, rest-villains                 | Baixa                  |
| Perfil Maven `native`                               | Build           | Não                 | Todos os módulos                           | Baixa                  |
| Perfil Maven `it-coverage` separado                 | Build           | Não                 | Todos os módulos                           | Baixa                  |
| `quarkus-container-image-docker`                    | Infra           | Não (script shell)  | Todos os módulos                           | Baixa                  |
| Kubernetes/OpenShift manifests gerados              | Infra           | Não                 | Todos os módulos                           | Baixa\*                |
| Contract Testing (Pact)                             | Testes          | Não                 | 4 módulos                                  | Baixa\*                |
| Playwright UI Tests                                 | Testes          | Não                 | rest-heroes, rest-villains                 | Baixa\*                |
| WireMock                                            | Testes          | Não                 | rest-narration, rest-fights                | Baixa\*                |
| `@TestProfile`                                      | Testes          | Não                 | rest-narration, rest-fights                | Baixa                  |

> `*` Baixa porque requer mudança arquitetural ou não se aplica diretamente ao escopo atual do ms-demo.

---

## 10. Recomendações Priorizadas

As seguintes melhorias são diretamente aplicáveis ao ms-demo sem alterar o escopo do projeto
(aplicação única, H2, fins educacionais/profissionais):

### Prioridade Alta — Maior impacto com baixo esforço

Nenhuma pendência de alta prioridade neste momento (itens de alta prioridade já implementados).

### Prioridade Média — Boas práticas com esforço moderado

1. **Adicionar anotações MicroProfile OpenAPI completas** (`@Operation`, `@APIResponse`,
   `@Schema`, `@Tag`) nos recursos — melhora a qualidade do Swagger UI gerado.

2. **Usar `@ConfigMapping` para propriedades customizadas** — substitui `@ConfigProperty`
   individuais por uma interface tipada e validada.

3. **Adicionar `@RunOnVirtualThread`** nos resources bloqueantes — aproveita Java 21 Virtual Threads.

4. **Adicionar `quarkus.datasource.jdbc.telemetry=true`** e a
   dependência `opentelemetry-jdbc` — traces SQL no Jaeger.

5. **Adicionar `@ParameterizedTest`** nos casos de teste existentes para cobrir edge cases
    (nomes vazios, nulos, formatos inválidos).

### Prioridade Baixa — Refinamentos e polimento

1. Substituir `jacoco-maven-plugin` por `quarkus-jacoco` (test scope) para cobertura mais
    precisa com o classloader do Quarkus.

2. Adicionar `maven-surefire-junit5-tree-reporter` para melhor output do `mvn test`.

3. Adicionar `quarkus.jackson.serialization-inclusion=non-empty` para omitir campos nulos
    nas respostas.

4. Criar custom `HealthCheck` anotado com `@Liveness` para validar a camada HTTP.

5. Adicionar `banner.txt` customizado para identidade visual no startup.

---

_Relatório gerado em: 9 de abril de 2026_
_Versão analisada — quarkus-ms-demo: 1.0.1-SNAPSHOT (Quarkus 3.34.3)_
_Versão analisada — quarkus-super-heroes: 1.0 (Quarkus 3.34.2)_
