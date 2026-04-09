package org.acme.user.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.acme.user.resources.dtos.UserRequest;
import org.acme.user.resources.dtos.UserResponse;
import org.acme.user.services.UserService;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class UserResource {

  private final UserService service;

  @Context UriInfo uriInfo;

  @GET
  public List<UserResponse> list(
      @QueryParam("page") @DefaultValue("0") @Min(0) int page,
      @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size,
      @QueryParam("sort") @DefaultValue("username") String sortBy,
      @QueryParam("direction") @DefaultValue("asc") String direction) {
    return service.list(page, size, sortBy, direction);
  }

  @GET
  @Path("/examples/named-query")
  public List<UserResponse> listByNamedQuery(@QueryParam("status") Integer userStatus) {
    return service.listByStatusNamedQuery(userStatus);
  }

  @GET
  @Path("/examples/named-native-query")
  public List<UserResponse> listByNamedNativeQuery(@QueryParam("emailDomain") String emailDomain) {
    return service.listByEmailDomainNativeQuery(emailDomain);
  }

  @GET
  @Path("/examples/criteria")
  public List<UserResponse> listByCriteria(
      @QueryParam("usernamePrefix") String usernamePrefix,
      @QueryParam("status") Integer userStatus,
      @QueryParam("emailDomain") String emailDomain) {
    return service.listByCriteria(usernamePrefix, userStatus, emailDomain);
  }

  @POST
  public Response create(@Valid UserRequest user) {
    UserResponse response = service.create(user);
    return Response.created(uriInfo.getAbsolutePathBuilder().path(response.username()).build())
        .entity(response)
        .build();
  }

  @POST
  @Path("/createWithArray")
  public Response createWithArray(@NotEmpty List<@Valid UserRequest> users) {
    return Response.status(Response.Status.CREATED).entity(service.createMany(users)).build();
  }

  @POST
  @Path("/createWithList")
  public Response createWithList(@NotEmpty List<@Valid UserRequest> users) {
    return Response.status(Response.Status.CREATED).entity(service.createMany(users)).build();
  }

  @GET
  @Path("/{username}")
  public UserResponse getByUsername(@PathParam("username") @NotBlank String username) {
    return service
        .getByUsername(username)
        .orElseThrow(() -> new NotFoundException("User not found: " + username));
  }

  @PUT
  @Path("/{username}")
  public Response update(
      @PathParam("username") @NotBlank String username, @Valid UserRequest user) {
    UserResponse response =
        service
            .update(username, user)
            .orElseThrow(() -> new NotFoundException("User not found: " + username));
    return Response.ok(response).build();
  }

  @DELETE
  @Path("/{username}")
  public Response delete(@PathParam("username") @NotBlank String username) {
    if (!service.delete(username)) {
      throw new NotFoundException("User not found: " + username);
    }
    return Response.noContent().build();
  }
}
