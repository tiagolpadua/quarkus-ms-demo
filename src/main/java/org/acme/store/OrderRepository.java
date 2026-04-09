package org.acme.store;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class OrderRepository {

  private final Map<Long, Order> orders = new LinkedHashMap<>();
  private final AtomicLong sequence = new AtomicLong();

  public OrderRepository() {
    save(
        new Order(
            1L, 1L, 1, OffsetDateTime.parse("2026-04-09T10:15:30Z").toString(), "placed", false));
  }

  public Order save(Order order) {
    Order copy = copyOf(order);
    if (copy.id() == null) {
      copy =
          new Order(
              sequence.incrementAndGet(),
              copy.petId(),
              copy.quantity(),
              copy.shipDate(),
              copy.status(),
              copy.complete());
    } else {
      sequence.accumulateAndGet(copy.id(), Math::max);
    }
    orders.put(copy.id(), copy);
    return copyOf(copy);
  }

  public Optional<Order> findById(Long id) {
    Order order = orders.get(id);
    return order == null ? Optional.empty() : Optional.of(copyOf(order));
  }

  public boolean delete(Long id) {
    return orders.remove(id) != null;
  }

  private Order copyOf(Order source) {
    return new Order(
        source.id(),
        source.petId(),
        source.quantity(),
        source.shipDate(),
        source.status(),
        source.complete());
  }
}
