package org.acme.store.mappers;

import java.util.List;
import org.acme.store.Order;
import org.acme.store.dtos.OrderRequest;
import org.acme.store.dtos.OrderResponse;

public final class OrderMapper {

  private OrderMapper() {}

  public static Order toEntity(OrderRequest source) {
    Order target = new Order();
    target.setId(source.id());
    target.setPetId(source.petId());
    target.setQuantity(source.quantity());
    target.setShipDate(source.shipDate());
    target.setStatus(source.status());
    target.setComplete(source.complete());
    return target;
  }

  public static OrderResponse toResponse(Order source) {
    return new OrderResponse(
        source.getId(),
        source.getPetId(),
        source.getQuantity(),
        source.getShipDate(),
        source.getStatus(),
        source.getComplete());
  }

  public static List<OrderResponse> toResponseList(List<Order> source) {
    return source.stream().map(OrderMapper::toResponse).toList();
  }
}
