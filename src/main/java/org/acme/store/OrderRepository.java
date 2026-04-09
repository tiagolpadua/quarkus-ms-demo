package org.acme.store;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.Optional;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {

  @Transactional
  public Order save(Order order) {
    if (order.getId() == null) {
      persist(order);
      return order;
    } else {
      return getEntityManager().merge(order);
    }
  }

  public Optional<Order> findOptionalById(Long id) {
    return findByIdOptional(id);
  }

  @Transactional
  public boolean delete(Long id) {
    return deleteById(id);
  }
}
