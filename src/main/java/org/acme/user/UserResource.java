package org.acme.user;

import jakarta.inject.Inject;
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
import org.acme.user.dtos.UserData;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

  @Inject UserService service;

  @GET
  public List<UserData> list() {
    return service.list();
  }

  @POST
  public Response create(UserData user) {
    service.create(user);
    return Response.ok().build();
  }

  @POST
  @Path("/createWithArray")
  public Response createWithArray(List<UserData> users) {
    service.createMany(users);
    return Response.ok().build();
  }

  @POST
  @Path("/createWithList")
  public Response createWithList(List<UserData> users) {
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
  public Response update(@PathParam("username") String username, UserData user) {
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
