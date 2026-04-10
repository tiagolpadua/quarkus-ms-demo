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
import lombok.RequiredArgsConstructor;
import org.acme.store.resources.dtos.InventoryResponse;
import org.acme.store.resources.dtos.OrderRequest;
import org.acme.store.resources.dtos.OrderResponse;
import org.acme.store.services.StoreService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/store")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Store", description = "Operations for inventory and order lifecycle")
public class StoreResource {

  private final StoreService service;

  @Context UriInfo uriInfo;

  @GET
  @Path("/inventory")
  @Operation(summary = "Get pet inventory by status")
  @APIResponse(
      responseCode = "200",
      description = "Inventory response",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = InventoryResponse.class)))
  public InventoryResponse getInventory() {
    return service.getInventory();
  }

  @POST
  @Path("/order")
  @Operation(summary = "Place an order for a pet")
  @RequestBody(
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = OrderRequest.class)))
  @APIResponse(
      responseCode = "201",
      description = "Order created",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = OrderResponse.class)))
  @APIResponse(responseCode = "400", description = "Invalid request payload")
  public Response placeOrder(@Valid OrderRequest order) {
    OrderResponse response = service.placeOrder(order);
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
  @Operation(summary = "Find order by id")
  @APIResponse(
      responseCode = "200",
      description = "Order found",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = OrderResponse.class)))
  @APIResponse(responseCode = "404", description = "Order not found")
  public OrderResponse getOrderById(@PathParam("orderId") @Positive Long orderId) {
    return service
        .getOrderById(orderId)
        .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
  }

  @DELETE
  @Path("/order/{orderId}")
  @Operation(summary = "Delete an order by id")
  @APIResponse(responseCode = "204", description = "Order deleted")
  @APIResponse(responseCode = "404", description = "Order not found")
  public Response deleteOrder(@PathParam("orderId") @Positive Long orderId) {
    if (!service.deleteOrder(orderId)) {
      throw new NotFoundException("Order not found: " + orderId);
    }
    return Response.noContent().build();
  }
}
