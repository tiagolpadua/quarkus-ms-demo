package org.acme.store.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.acme.pet.persistence.Pet;
import org.acme.pet.persistence.PetRepository;
import org.acme.store.persistence.Order;
import org.acme.store.persistence.OrderRepository;
import org.acme.store.resources.dtos.OrderRequest;
import org.acme.store.services.mappers.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

  @Mock OrderRepository orderRepository;
  @Mock PetRepository petRepository;

  private StoreService service;

  @BeforeEach
  void setUp() {
    OrderMapper mapper = Mappers.getMapper(OrderMapper.class);
    service = new StoreService(orderRepository, petRepository, mapper);
  }

  @Test
  void shouldManageOrderLifecycle() {
    when(petRepository.listAll()).thenReturn(List.of(pet("available"), pet("pending")));
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            invocation -> {
              Order order = invocation.getArgument(0);
              if (order.getId() == null) {
                order.setId(55L);
              }
              return order;
            });
    when(orderRepository.findOptionalById(55L))
        .thenReturn(Optional.of(order(55L, 1L, 2, "approved", true)), Optional.empty());
    when(orderRepository.delete(55L)).thenReturn(true);

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
  }

  @ParameterizedTest
  @ValueSource(longs = {999999L, 888888L, 777777L})
  void shouldReturnFalseWhenDeletingUnknownOrder(long orderId) {
    when(orderRepository.delete(orderId)).thenReturn(false);
    assertThat(service.deleteOrder(orderId)).isFalse();
  }

  private Pet pet(String status) {
    Pet pet = new Pet();
    pet.setStatus(status);
    return pet;
  }

  private Order order(Long id, Long petId, Integer quantity, String status, Boolean complete) {
    Order order = new Order();
    order.setId(id);
    order.setPetId(petId);
    order.setQuantity(quantity);
    order.setShipDate(OffsetDateTime.parse("2026-04-09T12:00:00Z"));
    order.setStatus(status);
    order.setComplete(complete);
    return order;
  }
}
