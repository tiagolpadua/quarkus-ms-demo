package org.acme.user;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.acme.user.dtos.UserRequest;
import org.acme.user.dtos.UserResponse;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class UserResource {

  private final UserService service;

  @GET
  public List<UserResponse> list() {
    return service.list();
  }

  @POST
  public UserResponse create(UserRequest user) {
    return service.create(user);
  }

  @POST
  @Path("/createWithArray")
  public List<UserResponse> createWithArray(List<UserRequest> users) {
    return service.createMany(users);
  }

  @POST
  @Path("/createWithList")
  public List<UserResponse> createWithList(List<UserRequest> users) {
    return service.createMany(users);
  }

  @GET
  @Path("/{username}")
  public UserResponse getByUsername(@PathParam("username") String username) {
    return service
        .getByUsername(username)
        .orElseThrow(() -> new NotFoundException("User not found: " + username));
  }

  @PUT
  @Path("/{username}")
  public Response update(@PathParam("username") String username, UserRequest user) {
    UserResponse response =
        service
            .update(username, user)
            .orElseThrow(() -> new NotFoundException("User not found: " + username));
    return Response.ok(response).build();
  }

  @DELETE
  @Path("/{username}")
  public Response delete(@PathParam("username") String username) {
    if (!service.delete(username)) {
      throw new NotFoundException("User not found: " + username);
    }
    return Response.noContent().build();
  }
}
