package org.acme.store.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreResourceTest {

  @Test
  void testInventoryAndOrderLifecycle() {
    given()
        .when()
        .get("/store/inventory")
        .then()
        .statusCode(200)
        .body("inventory.available", is(1), "inventory.pending", is(1));

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
            .statusCode(201)
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

  @Test
  void testOrderValidation() {
    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "petId": 0,
              "quantity": 0,
              "shipDate": null,
              "status": "",
              "complete": null
            }
            """)
        .when()
        .post("/store/order")
        .then()
        .statusCode(400)
        .contentType(containsString("application/problem+json"));
  }

  @Test
  void testOrderRejectsInvalidShipDateFormat() {
    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "petId": 1,
              "quantity": 1,
              "shipDate": "not-a-date",
              "status": "placed",
              "complete": true
            }
            """)
        .when()
        .post("/store/order")
        .then()
        .statusCode(400);
  }

  @Test
  void testDeleteNonExistingOrderReturnsNotFound() {
    given()
        .when()
        .delete("/store/order/999999")
        .then()
        .statusCode(404)
        .contentType(containsString("application/problem+json"));
  }

  @Test
  void testConcurrentOrderCreation() {
    List<CompletableFuture<Integer>> requests =
        List.of(
            createOrderAsync("2026-04-10T10:00:00Z"),
            createOrderAsync("2026-04-10T10:00:01Z"),
            createOrderAsync("2026-04-10T10:00:02Z"));

    List<Integer> statuses = requests.stream().map(future -> future.join()).toList();

    org.junit.jupiter.api.Assertions.assertTrue(
        statuses.stream().allMatch(code -> code == 201),
        "Expected all concurrent creates to return 201");
  }

  private CompletableFuture<Integer> createOrderAsync(String shipDate) {
    return CompletableFuture.supplyAsync(
        () ->
            given()
                .contentType(ContentType.JSON)
                .body(
                    """
                    {
                      "petId": 1,
                      "quantity": 1,
                      "shipDate": "%s",
                      "status": "placed",
                      "complete": true
                    }
                    """
                        .formatted(shipDate))
                .when()
                .post("/store/order")
                .then()
                .extract()
                .statusCode(),
        CompletableFuture.delayedExecutor(0, TimeUnit.MILLISECONDS));
  }
}
