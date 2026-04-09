package org.acme.store;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.acme.pet.persistence.Pet;
import org.acme.pet.persistence.PetRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class StoreService {

  private final OrderRepository orderRepository;
  private final PetRepository petRepository;

  public Map<String, Integer> getInventory() {
    Map<String, Integer> inventory = new LinkedHashMap<>();
    for (Pet pet : petRepository.listAll()) {
      inventory.merge(pet.getStatus(), 1, Integer::sum);
    }
    return inventory;
  }

  public Order placeOrder(Order order) {
    return orderRepository.save(order);
  }

  public Optional<Order> getOrderById(Long orderId) {
    return orderRepository.findOptionalById(orderId);
  }

  public boolean deleteOrder(Long orderId) {
    return orderRepository.delete(orderId);
  }
}
