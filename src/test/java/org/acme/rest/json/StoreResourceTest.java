package org.acme.rest.json;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreResourceTest {

  @Test
  public void testInventoryAndOrderLifecycle() {
    given()
        .when()
        .get("/store/inventory")
        .then()
        .statusCode(200)
        .body("available", is(1), "pending", is(1));

    Number orderIdValue =
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "petId": 1,
                  "quantity": 2,
                  "shipDate": "2026-04-09T12:00:00Z",
                  "status": "approved",
                  "complete": true
                }
                """)
            .when()
            .post("/store/order")
            .then()
            .statusCode(200)
            .body("id", is(2), "status", is("approved"), "complete", is(true))
            .extract()
            .path("id");
    long orderId = orderIdValue.longValue();

    given()
        .when()
        .get("/store/order/" + orderId)
        .then()
        .statusCode(200)
        .body("petId", is(1), "quantity", is(2));

    given().when().delete("/store/order/" + orderId).then().statusCode(204);

    given()
        .when()
        .get("/store/order/" + orderId)
        .then()
        .statusCode(404)
        .contentType(containsString("application/problem+json"));
  }
}
