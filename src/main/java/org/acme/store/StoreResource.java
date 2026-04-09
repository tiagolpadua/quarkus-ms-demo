package org.acme.store;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import lombok.RequiredArgsConstructor;

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
  public Order placeOrder(Order order) {
    return service.placeOrder(order);
  }

  @GET
  @Path("/order/{orderId}")
  public Response getOrderById(@PathParam("orderId") Long orderId) {
    return service
        .getOrderById(orderId)
        .map(order -> Response.ok(order).build())
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  @DELETE
  @Path("/order/{orderId}")
  public Response deleteOrder(@PathParam("orderId") Long orderId) {
    return service.deleteOrder(orderId)
        ? Response.noContent().build()
        : Response.status(Response.Status.NOT_FOUND).build();
  }
}
