package org.acme.store.services.mappers;

import java.util.List;
import org.acme.store.persistence.Order;
import org.acme.store.resources.dtos.OrderRequest;
import org.acme.store.resources.dtos.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface OrderMapper {

  Order toEntity(OrderRequest source);

  OrderResponse toResponse(Order source);

  List<OrderResponse> toResponseList(List<Order> source);
}
