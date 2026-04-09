package org.acme.rest.json;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class OpenApiResourceTest {

  @Test
  void testOpenApiDocument() {
    given()
        .when()
        .get("/q/openapi")
        .then()
        .statusCode(200)
        .body(containsString("openapi: 3."))
        .body(containsString("title: Swagger Petstore"))
        .body(containsString("/pet"))
        .body(containsString("/store"))
        .body(containsString("/user"));
  }

  @Test
  void testSwaggerUi() {
    given()
        .when()
        .get("/q/swagger-ui/")
        .then()
        .statusCode(200)
        .body(containsString("OpenAPI UI"))
        .body(containsString("id=\"swagger-ui\""));
  }

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
}
