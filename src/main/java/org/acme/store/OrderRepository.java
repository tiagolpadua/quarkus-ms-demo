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
        Order.builder()
            .id(1L)
            .petId(1L)
            .quantity(1)
            .shipDate(OffsetDateTime.parse("2026-04-09T10:15:30Z").toString())
            .status("placed")
            .complete(false)
            .build());
  }

  public Order save(Order order) {
    Order copy = copyOf(order);
    if (copy.getId() == null) {
      copy.setId(sequence.incrementAndGet());
    } else {
      sequence.accumulateAndGet(copy.getId(), Math::max);
    }
    orders.put(copy.getId(), copy);
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
    return source.toBuilder().build();
  }
}
