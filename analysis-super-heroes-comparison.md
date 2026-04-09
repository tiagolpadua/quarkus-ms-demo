# Análise Comparativa: quarkus-ms-demo vs quarkus-super-heroes

> **Objetivo:** Identificar bibliotecas, padrões e boas práticas presentes no projeto
> [quarkus-super-heroes](https://github.com/quarkusio/quarkus-super-heroes) que ainda não foram
> adotados no `quarkus-ms-demo`. Esta análise cobre todos os módulos do projeto de referência:
> `rest-heroes`, `rest-villains`, `rest-fights`, `rest-narration`, `event-statistics`, e
> `grpc-locations`.
>
> **Importante:** Este documento foi atualizado para refletir o estado atual do projeto,
> removendo da lista de pendencias os itens ja implementados.

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
| Cobertura      | `jacoco-maven-plugin` + `quarkus-jacoco`       |
| Formatação     | Spotless + Google Java Format                  |

### quarkus-super-heroes

| Módulo             | Descripção                     | Linguagem | Stack de Persistência                                |
| ------------------ | ------------------------------ | --------- | ---------------------------------------------------- |
| `rest-heroes`      | CRUD de heróis                 | Java      | Hibernate Reactive Panache + PostgreSQL              |
| `rest-villains`    | CRUD de vilões                 | Java      | Hibernate ORM Panache + PostgreSQL                   |
| `rest-fights`      | Batalhas entre heróis e vilões | Java      | MongoDB Panache + Kafka + gRPC client                |
| `rest-narration`   | Narração com AI                | Java      | Nenhuma (LangChain4j + OpenAI/Azure)                 |
| `event-statistics` | Estatísticas via eventos       | Java      | Nenhuma (Kafka + WebSockets)                         |
| `grpc-locations`   | Serviço gRPC de localizações   | Kotlin    | Hibernate ORM Panache Kotlin + MariaDB               |

---

## 2. Testes e Qualidade

Secao concluida no `quarkus-ms-demo`.

Todos os itens comparados nesta secao ja foram implementados e removidos da lista de pendencias.

## 3. Padrões de Código e Boas Práticas

### 3.1 @ConfigMapping — Ausente no ms-demo

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

### 3.2 Application Banner Customizado — Ausente no ms-demo

**Módulos com uso:** `rest-heroes`, `rest-villains`.

O super-heroes define um `banner.txt` customizado em `src/main/resources/` exibido no startup.

```properties
quarkus.banner.path=banner.txt
```

---

### 3.3 Clients REST Tipados (@RegisterRestClient) — Ausente no ms-demo

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

---

## 4. Observabilidade Avançada

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

### 5.2 Perfis Maven de Build — Parcialmente implementado no ms-demo

**Módulos com uso:** todos os módulos do super-heroes.

O super-heroes define perfis Maven para cenarios especificos.

No `quarkus-ms-demo`, o perfil `it-coverage` ja foi implementado.
A principal pendencia desta categoria e o perfil `native`.

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
| `@ConfigMapping` tipado                             | Configuração    | Não                 | rest-villains, rest-fights                 | Média                  |
| `quarkus-smallrye-fault-tolerance`                  | Resiliência     | Não                 | rest-fights, rest-narration                | Baixa\*                |
| CORS habilitado                                     | Configuração    | Não                 | rest-heroes, rest-villains                 | Baixa\*                |
| `quarkus-config-yaml`                               | Configuração    | Não                 | rest-heroes, grpc-locations                | Baixa                  |
| `banner.txt` customizado                            | DX              | Não                 | rest-heroes, rest-villains                 | Baixa                  |
| Perfil Maven `native`                               | Build           | Não                 | Todos os módulos                           | Baixa                  |
| `quarkus-container-image-docker`                    | Infra           | Não (script shell)  | Todos os módulos                           | Baixa                  |
| Kubernetes/OpenShift manifests gerados              | Infra           | Não                 | Todos os módulos                           | Baixa\*                |

> `*` Baixa porque requer mudança arquitetural ou não se aplica diretamente ao escopo atual do ms-demo.

---

## 10. Recomendações Priorizadas

As seguintes melhorias são diretamente aplicáveis ao ms-demo sem alterar o escopo do projeto
(aplicação única, H2, fins educacionais/profissionais):

### Prioridade Alta — Maior impacto com baixo esforço

Nenhuma pendência de alta prioridade neste momento (itens de alta prioridade já implementados).

### Prioridade Média — Boas práticas com esforço moderado

1. **Usar `@ConfigMapping` para propriedades customizadas** — substitui `@ConfigProperty`
   individuais por uma interface tipada e validada.

### Prioridade Baixa — Refinamentos e polimento

1. Adicionar `banner.txt` customizado para identidade visual no startup.

---

_Relatório gerado em: 9 de abril de 2026_
_Versão analisada — quarkus-ms-demo: 1.0.1-SNAPSHOT (Quarkus 3.34.3)_
_Versão analisada — quarkus-super-heroes: 1.0 (Quarkus 3.34.2)_
