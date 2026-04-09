package org.acme.store;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.acme.store.dtos.OrderRequest;
import org.acme.store.dtos.OrderResponse;
import org.acme.store.mappers.OrderMapper;

@Path("/store")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class StoreResource {

  private final StoreService service;

  @GET
  @Path("/inventory")
  public Map<String, Integer> getInventory() {
    return service.getInventory();
  }

  @POST
  @Path("/order")
  public OrderResponse placeOrder(OrderRequest order) {
    return OrderMapper.toResponse(service.placeOrder(OrderMapper.toEntity(order)));
  }

  @GET
  @Path("/order/{orderId}")
  public OrderResponse getOrderById(@PathParam("orderId") Long orderId) {
    return service
        .getOrderById(orderId)
        .map(OrderMapper::toResponse)
        .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
  }

  @DELETE
  @Path("/order/{orderId}")
  public Response deleteOrder(@PathParam("orderId") Long orderId) {
    if (!service.deleteOrder(orderId)) {
      throw new NotFoundException("Order not found: " + orderId);
    }
    return Response.noContent().build();
  }
}
