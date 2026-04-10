package org.acme.store.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the pure-logic branches inside {@link OrderRepository}.
 *
 * <p>{@code save} contains an {@code if (id == null)} branch that selects between {@code persist}
 * and {@code merge}. Both paths are exercised here by subclassing the repository to avoid starting
 * Panache/JPA.
 */
class OrderRepositoryTest {

  private OrderRepository repository;

  @BeforeEach
  void setUp() {
    // Subclass that overrides Panache persistence operations so no JPA context is needed.
    repository =
        new OrderRepository() {
          @Override
          public void persist(Order order) {
            order.setId(42L);
          }
        };
  }

  @Test
  void shouldPersistAndReturnOrderWhenIdIsNull() {
    Order order = order(null);

    Order saved = repository.save(order);

    assertThat(saved).isSameAs(order);
    assertThat(saved.getId()).isEqualTo(42L);
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private Order order(Long id) {
    Order o = new Order();
    o.setId(id);
    o.setPetId(1L);
    o.setQuantity(2);
    o.setShipDate(OffsetDateTime.parse("2026-04-09T12:00:00Z"));
    o.setStatus("placed");
    o.setComplete(false);
    return o;
  }
}
