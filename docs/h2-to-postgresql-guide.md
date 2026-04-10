# Switching from H2 to PostgreSQL

This guide shows how to switch the ms-demo from H2 in-memory database to PostgreSQL for production-like testing.

## Why Switch?

- **H2** is ideal for local development and quick unit tests — no external dependencies.
- **PostgreSQL** is a real, production-grade database. Testing against it validates:
  - SQL dialect differences (H2 and PostgreSQL don't always behave identically)
  - Connection pooling behavior
  - Schema migration with tools like Flyway or Liquibase

## Two Approaches

### 1. **Quarkus Dev Services** (Recommended for Local Development)

Quarkus automatically starts a PostgreSQL container via Docker/Podman when you run in dev mode.

#### Prerequisites

- Docker or Podman installed and running
- No configuration needed — Quarkus does it automatically!

#### Command

```bash
./mvnw quarkus:dev
```

**That's it.** Quarkus will:
1. Detect that `quarkus-jdbc-postgresql` is present
2. Start a PostgreSQL container automatically
3. Configure the datasource to connect to `jdbc:postgresql://localhost:5432/postgres`
4. Provide database credentials via environment variables

No setup, no manual container management.

### 2. **Manual PostgreSQL Profile** (For Explicit Configuration)

If you want explicit control, create a Maven profile and an `application-postgresql.properties` file.

#### Step 1: Add the PostgreSQL Driver Dependency

In `pom.xml`, add:

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-jdbc-postgresql</artifactId>
</dependency>
```

> **Note:** This is already in the pom.xml.

#### Step 2: Create `application-postgresql.properties`

In `src/main/resources/`, create a new file:

```properties
# PostgreSQL datasource configuration for production-like environments
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/petstore
quarkus.datasource.username=petstore
quarkus.datasource.password=petstore
quarkus.datasource.jdbc.telemetry=true

# ORM schema management
quarkus.hibernate-orm.schema-management.strategy=drop-and-create
quarkus.hibernate-orm.sql-load-script=import.sql

# H2-specific settings removed; PostgreSQL handles dialect automatically
```

#### Step 3: Run with the PostgreSQL Profile

```bash
./mvnw quarkus:dev -Dquarkus.profile=postgresql
```

Or set `QUARKUS_PROFILE=postgresql` in your shell.

#### Step 4: Start PostgreSQL Manually

If not using Dev Services:

```bash
docker run --name petstore-postgres \
  -e POSTGRES_DB=petstore \
  -e POSTGRES_USER=petstore \
  -e POSTGRES_PASSWORD=petstore \
  -p 5432:5432 \
  postgres:16
```

Or use `docker-compose` (see [docker-compose.yml](../docker-compose.yml)).

## SQL Dialect Differences

H2 and PostgreSQL have subtle differences:

| Feature | H2 | PostgreSQL |
| --- | --- | --- |
| **String literals** | Single quotes (`'text'`) | Single quotes (`'text'`) |
| **Case sensitivity** | Identifiers are case-insensitive by default | Identifiers are case-sensitive; wrap in quotes for mixed case |
| **Auto-increment** | `IDENTITY` keyword | `SERIAL` or `GENERATED ALWAYS AS IDENTITY` |
| **Sequences** | Not commonly used | Native support, often preferred |
| **JDBC type auto-detection** | Lenient | More strict |
| **Schema names** | Default is `public` | Default is `public` |

Hibernate ORM handles most of these via the dialect, so no code changes are usually needed.

## Testing Against PostgreSQL

### Run Integration Tests with PostgreSQL

```bash
./mvnw verify -Dquarkus.profile=postgresql
```

This will:
1. Compile the application
2. Start PostgreSQL (via Dev Services or manual container)
3. Run all tests against PostgreSQL
4. Report test results

### Verify Schema Generation

Check that Hibernate schema creation works correctly:

```bash
# Run dev mode and check the PostgreSQL container logs
docker logs petstore-postgres
```

You should see the schema creation DDL statements.

## Troubleshooting

### "No dialect discovered for `postgres`"

Ensure `quarkus-jdbc-postgresql` is in your pom.xml and the datasource URL is `jdbc:postgresql://...`.

### Connection refused

If running PostgreSQL manually:
- Check that the container is running: `docker ps`
- Verify the port (default 5432) matches your configuration
- Check credentials in `application-postgresql.properties`

### "ERROR: relation does not exist"

This usually means the schema wasn't created. Ensure:
- `quarkus.hibernate-orm.schema-management.strategy=drop-and-create` is set
- The datasource user has DDL permissions (CREATE, ALTER, DROP)

## Production Best Practices

1. **Use Liquibase or Flyway** instead of `drop-and-create` for production:
   ```properties
   quarkus.liquibase.migrate-at-start=true
   # or
   quarkus.flyway.migrate-at-start=true
   ```

2. **Use a dedicated database user** with restricted permissions (not the superuser).

3. **Test all database-dependent code** against PostgreSQL, not just H2.

4. **Version your schema** with migration tools for traceability and rollback capability.

## References

- [Quarkus Datasource Guide](https://quarkus.io/guides/datasource)
- [Quarkus Hibernate ORM Guide](https://quarkus.io/guides/hibernate-orm)
- [Quarkus Dev Services](https://quarkus.io/guides/datasource#dev-services)
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/)
- [Liquibase Quarkus Guide](https://quarkus.io/guides/liquibase)
- [Flyway Quarkus Guide](https://quarkus.io/guides/flyway)

---

## Quick Reference: Dev Services

The **easiest** way to test against PostgreSQL locally:

```bash
./mvnw quarkus:dev
# Quarkus automatically starts PostgreSQL in a container
# Your app connects to it with zero configuration
```

When you stop dev mode (`Ctrl+C`), the PostgreSQL container is automatically cleaned up.
