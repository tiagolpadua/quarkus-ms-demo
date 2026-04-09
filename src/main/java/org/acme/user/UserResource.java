package org.acme.user;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.acme.user.dtos.UserDto;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class UserResource {

  private final UserService service;

  @GET
  public List<UserDto> list() {
    return service.list();
  }

  @POST
  public Response create(UserDto user) {
    service.create(user);
    return Response.ok().build();
  }

  @POST
  @Path("/createWithArray")
  public Response createWithArray(List<UserDto> users) {
    service.createMany(users);
    return Response.ok().build();
  }

  @POST
  @Path("/createWithList")
  public Response createWithList(List<UserDto> users) {
    service.createMany(users);
    return Response.ok().build();
  }

  @GET
  @Path("/{username}")
  public Response getByUsername(@PathParam("username") String username) {
    return service
        .getByUsername(username)
        .map(user -> Response.ok(user).build())
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  @PUT
  @Path("/{username}")
  public Response update(@PathParam("username") String username, UserDto user) {
    return service
        .update(username, user)
        .map(updatedUser -> Response.ok(updatedUser).build())
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  @DELETE
  @Path("/{username}")
  public Response delete(@PathParam("username") String username) {
    return service.delete(username)
        ? Response.noContent().build()
        : Response.status(Response.Status.NOT_FOUND).build();
  }
}
