package org.acme.store.resources;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.time.OffsetDateTime;
import java.util.Map;
import org.acme.store.resources.dtos.InventoryResponse;
import org.acme.store.resources.dtos.OrderRequest;
import org.acme.store.resources.dtos.OrderResponse;
import org.acme.store.services.StoreService;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreResourceMockTest {

  @InjectMock StoreService service;

  @Test
  void shouldUseServiceForInventoryAndOrder() {
    when(service.getInventory())
        .thenReturn(new InventoryResponse(Map.of("available", 3, "pending", 1)));
    when(service.placeOrder(org.mockito.ArgumentMatchers.any(OrderRequest.class)))
        .thenReturn(
            new OrderResponse(
                55L, 1L, 2, OffsetDateTime.parse("2026-04-09T12:00:00Z"), "approved", true));

    given()
        .when()
        .get("/store/inventory")
        .then()
        .statusCode(200)
        .body("inventory.available", org.hamcrest.CoreMatchers.is(3));

    given()
        .contentType("application/json")
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
        .body("id", org.hamcrest.CoreMatchers.is(55));

    verify(service).getInventory();
    verify(service).placeOrder(org.mockito.ArgumentMatchers.any(OrderRequest.class));
  }

  @Test
  void shouldReturnNotFoundWhenDeleteFails() {
    when(service.deleteOrder(77L)).thenReturn(false);

    given().when().delete("/store/order/77").then().statusCode(404);

    verify(service).deleteOrder(77L);
  }
}
