package org.acme.store.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.acme.store.dtos.OrderRequest;
import org.acme.store.dtos.OrderResponse;
import org.acme.store.mappers.OrderMapper;
import org.acme.store.services.StoreService;

@Path("/store")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class StoreResource {

  private final StoreService service;

  @Context UriInfo uriInfo;

  @GET
  @Path("/inventory")
  public Map<String, Integer> getInventory() {
    return service.getInventory();
  }

  @POST
  @Path("/order")
  public Response placeOrder(@Valid OrderRequest order) {
    OrderResponse response =
        OrderMapper.toResponse(service.placeOrder(OrderMapper.toEntity(order)));
    return Response.created(
            uriInfo
                .getBaseUriBuilder()
                .path("store/order")
                .path(String.valueOf(response.id()))
                .build())
        .entity(response)
        .build();
  }

  @GET
  @Path("/order/{orderId}")
  public OrderResponse getOrderById(@PathParam("orderId") @Positive Long orderId) {
    return service
        .getOrderById(orderId)
        .map(OrderMapper::toResponse)
        .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
  }

  @DELETE
  @Path("/order/{orderId}")
  public Response deleteOrder(@PathParam("orderId") @Positive Long orderId) {
    if (!service.deleteOrder(orderId)) {
      throw new NotFoundException("Order not found: " + orderId);
    }
    return Response.noContent().build();
  }
}
