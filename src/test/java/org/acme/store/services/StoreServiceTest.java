package org.acme.store.services;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import org.acme.store.resources.dtos.OrderRequest;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestTransaction
class StoreServiceTest {

  @Inject StoreService service;

  @Test
  void shouldManageOrderLifecycle() {
    var inventory = service.getInventory();

    assertThat(inventory.inventory()).isNotNull();
    assertThat(inventory.inventory()).isNotEmpty();

    OrderRequest request =
        new OrderRequest(
            null, 1L, 2, OffsetDateTime.parse("2026-04-09T12:00:00Z"), "approved", true);

    var created = service.placeOrder(request);

    assertThat(created.id()).isNotNull();
    assertThat(created.petId()).isEqualTo(1L);
    assertThat(created.quantity()).isEqualTo(2);
    assertThat(created.status()).isEqualTo("approved");

    assertThat(service.getOrderById(created.id())).isPresent();

    assertThat(service.deleteOrder(created.id())).isTrue();
    assertThat(service.getOrderById(created.id())).isEmpty();
    assertThat(service.deleteOrder(999999L)).isFalse();
  }
}
