package org.acme.store.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import org.acme.store.resources.dtos.InventoryResponse;
import org.acme.store.resources.dtos.OrderRequest;
import org.acme.store.resources.dtos.OrderResponse;
import org.acme.store.services.StoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Demonstrates {@code @InjectMocks} and {@code @ParameterizedTest} with {@code @CsvSource}.
 *
 * <p>{@code @InjectMocks} instantiates {@link StoreResource} and injects the {@code @Mock} {@link
 * StoreService} automatically, without requiring a manual constructor call in {@code setUp()}.
 *
 * <p>{@code @CsvSource} is an alternative to {@code @ValueSource} when you need multiple parameters
 * per test case. Each string becomes a row of comma-separated arguments.
 */
@ExtendWith(MockitoExtension.class)
class StoreResourceMockTest {

  @Mock StoreService storeService;

  @InjectMocks StoreResource storeResource;

  @Test
  void shouldReturnInventoryFromMockedService() {
    InventoryResponse inventory = new InventoryResponse(Map.of("available", 3, "pending", 1));
    when(storeService.getInventory()).thenReturn(inventory);

    assertThat(storeResource.getInventory()).isEqualTo(inventory);
    verify(storeService).getInventory();
  }

  @Test
  void shouldCreateOrderAndReturnLocation() {
    UriInfo uriInfo = org.mockito.Mockito.mock(UriInfo.class);
    when(uriInfo.getBaseUriBuilder()).thenAnswer(inv -> UriBuilder.fromUri("http://localhost/"));
    storeResource.uriInfo = uriInfo;

    OrderRequest request =
        new OrderRequest(null, 1L, 2, OffsetDateTime.parse("2026-01-01T00:00:00Z"), "placed", true);
    OrderResponse response =
        new OrderResponse(5L, 1L, 2, OffsetDateTime.parse("2026-01-01T00:00:00Z"), "placed", true);

    when(storeService.placeOrder(request)).thenReturn(response);

    var result = storeResource.placeOrder(request);

    assertThat(result.getStatus()).isEqualTo(201);
    assertThat(result.getHeaderString("Location")).isEqualTo("http://localhost/store/order/5");
  }

  @Test
  void shouldThrowNotFoundWhenOrderIsMissing() {
    when(storeService.getOrderById(77L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> storeResource.getOrderById(77L))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("77");
  }

  /**
   * {@code @CsvSource} — each string is one test case with comma-separated arguments.
   *
   * <p>Useful when you need to vary multiple inputs together (e.g., an order id and its expected
   * status string) rather than a single scalar value.
   */
  @ParameterizedTest
  @CsvSource({"100, placed", "200, approved", "300, delivered"})
  void shouldReturnOrderForDifferentStatuses(long orderId, String status) {
    OrderResponse order =
        new OrderResponse(
            orderId, 1L, 1, OffsetDateTime.parse("2026-01-01T00:00:00Z"), status, true);
    when(storeService.getOrderById(orderId)).thenReturn(Optional.of(order));

    assertThat(storeResource.getOrderById(orderId).status()).isEqualTo(status);
  }
}
