package org.acme.store.services;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.acme.pet.persistence.Pet;
import org.acme.pet.persistence.PetRepository;
import org.acme.store.persistence.Order;
import org.acme.store.persistence.OrderRepository;
import org.acme.store.resources.dtos.InventoryResponse;
import org.acme.store.resources.dtos.OrderRequest;
import org.acme.store.resources.dtos.OrderResponse;
import org.acme.store.services.mappers.OrderMapper;

@ApplicationScoped
@RequiredArgsConstructor
public class StoreService {

  private final OrderRepository orderRepository;
  private final PetRepository petRepository;
  private final OrderMapper mapper;

  @WithSpan("StoreService.getInventory")
  public InventoryResponse getInventory() {
    Map<String, Integer> inventory = new LinkedHashMap<>();
    for (Pet pet : petRepository.listAll()) {
      inventory.merge(pet.getStatus(), 1, Integer::sum);
    }
    return new InventoryResponse(inventory);
  }

  @Transactional
  @WithSpan("StoreService.placeOrder")
  public OrderResponse placeOrder(OrderRequest orderRequest) {
    Order order = mapper.toEntity(orderRequest);
    return mapper.toResponse(orderRepository.save(order));
  }

  @WithSpan("StoreService.getOrderById")
  public Optional<OrderResponse> getOrderById(@SpanAttribute("arg.orderId") Long orderId) {
    return orderRepository.findOptionalById(orderId).map(mapper::toResponse);
  }

  @Transactional
  @WithSpan("StoreService.deleteOrder")
  public boolean deleteOrder(@SpanAttribute("arg.orderId") Long orderId) {
    return orderRepository.delete(orderId);
  }
}
