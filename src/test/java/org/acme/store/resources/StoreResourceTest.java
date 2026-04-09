package org.acme.store.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import org.acme.store.resources.dtos.InventoryResponse;
import org.acme.store.resources.dtos.OrderRequest;
import org.acme.store.resources.dtos.OrderResponse;
import org.acme.store.services.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StoreResourceTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Mock StoreService service;

  private StoreResource resource;

  @BeforeEach
  void setUp() {
    resource = new StoreResource(service);
  }

  @Test
  void shouldManageInventoryAndOrderLifecycle() {
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getBaseUriBuilder())
        .thenAnswer(invocation -> UriBuilder.fromUri("http://localhost/"));
    resource.uriInfo = uriInfo;

    InventoryResponse inventory = new InventoryResponse(Map.of("available", 1, "pending", 1));
    OrderRequest orderRequest =
        new OrderRequest(
            null, 1L, 2, OffsetDateTime.parse("2026-04-09T12:00:00Z"), "approved", true);
    OrderResponse orderResponse =
        new OrderResponse(
            2L, 1L, 2, OffsetDateTime.parse("2026-04-09T12:00:00Z"), "approved", true);

    when(service.getInventory()).thenReturn(inventory);
    when(service.placeOrder(orderRequest)).thenReturn(orderResponse);
    when(service.getOrderById(2L)).thenReturn(Optional.of(orderResponse), Optional.empty());
    when(service.deleteOrder(2L)).thenReturn(true);

    assertThat(resource.getInventory()).isEqualTo(inventory);

    Response createResponse = resource.placeOrder(orderRequest);
    assertThat(createResponse.getStatus()).isEqualTo(201);
    assertThat(createResponse.getHeaderString("Location"))
        .isEqualTo("http://localhost/store/order/2");
    assertThat(createResponse.getEntity()).isEqualTo(orderResponse);

    assertThat(resource.getOrderById(2L)).isEqualTo(orderResponse);

    Response deleteResponse = resource.deleteOrder(2L);
    assertThat(deleteResponse.getStatus()).isEqualTo(204);

    assertThatThrownBy(() -> resource.getOrderById(2L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Order not found: 2");

    verify(service).getInventory();
    verify(service).placeOrder(orderRequest);
    verify(service, org.mockito.Mockito.times(2)).getOrderById(2L);
    verify(service).deleteOrder(2L);
  }

  @Test
  void shouldThrowWhenOrderIsMissing() {
    when(service.deleteOrder(999999L)).thenReturn(false);

    assertThatThrownBy(() -> resource.deleteOrder(999999L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Order not found: 999999");
  }

  @Test
  void shouldValidateOrderRequestWithoutHttpLayer() {
    OrderRequest invalidRequest = new OrderRequest(null, 0L, 0, null, "", null);

    var violations = validator.validate(invalidRequest);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString())
        .contains("petId", "quantity", "shipDate", "status", "complete");
  }
}
