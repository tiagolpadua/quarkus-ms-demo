# Plan: Implement Remaining Items from ARCHITECTURE_ANALYSIS.md

## Context

The ARCHITECTURE_ANALYSIS.md documents a senior architectural review of the project. ~90% of recommendations are already implemented. Three items remain pending:

1. **`.github/copilot-instructions.md` is stale** — still references `org.acme.rest.json` as the main package, but the project was refactored to domain-driven packages (`org.acme.pet`, `org.acme.store`, `org.acme.user`, `org.acme.shared`). This misleads AI assistants and new developers.
2. **No Makefile** — ARCHITECTURE_ANALYSIS.md section 4.3 lists a unified task runner as a nice-to-have. Convenience scripts exist (`.sh`/`.cmd`) but no `make` interface.
3. **OpenAPI contract tests are minimal** — `OpenApiResourceTest.java` validates title and path names only; no RFC 7807 error payload schema validation exists.

## Implementation Steps

### Step 1 — Update `.github/copilot-instructions.md` ✅

**File:** `.github/copilot-instructions.md`
**Action:** Full replacement.

Key changes:

- Replace `org.acme.rest.json` references with the correct domain package map (`org.acme.pet`, `org.acme.store`, `org.acme.user`, `org.acme.shared`)
- Document the internal domain structure: `resources/`, `resources/dtos/`, `services/`, `services/mappers/`, `persistence/`
- Add missing conventions: Java records for DTOs, Lombok `@RequiredArgsConstructor`, Bean Validation on request DTOs, RFC 7807 errors via `quarkus-resteasy-problem`, Spotless formatting
- Update test package paths to domain-specific (`org.acme.pet.resources`, etc.) + note that `org.acme.rest.json` is for cross-cutting tests only
- Expand "Useful Commands" to include all `.sh` scripts and their Maven equivalents

### Step 2 — Create `Makefile` at project root

**File:** `Makefile` (new)
**Action:** Create with targets that delegate to existing scripts.

```makefile
.DEFAULT_GOAL := help

.PHONY: help dev check fmt build docker

help: ## Show available targets
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  %-12s %s\n", $$1, $$2}'

dev: ## Start application in development mode (hot reload)
	./run.sh

check: ## Run tests and validate code formatting
	./run-check.sh

fmt: ## Auto-fix Java code formatting with Spotless
	./run-spotless-apply.sh

build: ## Build production artefacts with clean verify
	./run-build-prod.sh

docker: ## Build JVM Docker image and run the container
	./run-docker.sh
```

### Step 3 — Enhance `OpenApiResourceTest.java`

**File:** `src/test/java/org/acme/rest/json/OpenApiResourceTest.java`
**Action:** Add two new test methods + two imports.

**New imports:**

```java
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
```

**New test 1 — RFC 7807 error payload validation:**

```java
@Test
void testNonExistentPetReturnsRfc7807ProblemJson() {
  given()
      .when()
      .get("/pet/999999999")
      .then()
      .statusCode(404)
      .contentType(containsString("application/problem+json"))
      .body("status", equalTo(404))
      .body("title", notNullValue())
      .body("detail", notNullValue());
}
```

**New test 2 — Critical operations present in OpenAPI document:**

```java
@Test
void testOpenApiDocumentContainsCriticalOperations() {
  given()
      .when()
      .get("/q/openapi")
      .then()
      .statusCode(200)
      .body(containsString("/pet/{petId}"))
      .body(containsString("/store/order"))
      .body(containsString("/store/order/{orderId}"))
      .body(containsString("/user/{username}"));
}
```

## Critical Files

| File                                                        | Action                                           |
| ----------------------------------------------------------- | ------------------------------------------------ |
| `.github/copilot-instructions.md`                           | Full replacement                                 |
| `Makefile`                                                  | Create new file                                  |
| `src/test/java/org/acme/rest/json/OpenApiResourceTest.java` | Add 2 tests + 2 imports                          |
| `src/test/java/org/acme/pet/resources/PetResourceTest.java` | Reference only (pattern for RFC 7807 assertions) |

## Verification

```sh
# 1. Confirm new tests pass
./mvnw test -Dtest=OpenApiResourceTest

# 2. Confirm full suite still passes
./mvnw test

# 3. Confirm Spotless passes (runs automatically in validate phase)
./mvnw spotless:check

# 4. Confirm Makefile works
make help
make check
```
