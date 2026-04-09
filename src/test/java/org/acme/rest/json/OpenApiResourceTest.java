package org.acme.rest.json;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class OpenApiResourceTest {

  @Test
  public void testOpenApiDocument() {
    given()
        .when()
        .get("/q/openapi")
        .then()
        .statusCode(200)
        .body(containsString("openapi: 3."))
        .body(containsString("title: Quarkus MS Demo API"))
        .body(containsString("/fruits"))
        .body(containsString("/legumes"));
  }

  @Test
  public void testSwaggerUi() {
    given()
        .when()
        .get("/q/swagger-ui/")
        .then()
        .statusCode(200)
        .body(containsString("OpenAPI UI"))
        .body(containsString("id=\"swagger-ui\""));
  }
}
