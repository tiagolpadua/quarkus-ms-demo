package org.acme.rest.json;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/clients")
public class ClientResource {

  @GET
  public List<Client> list() {
    return Client.listAll();
  }

  @GET
  @Path("/{id}")
  public Response get(@PathParam("id") Long id) {
    Client client = Client.findById(id);
    if (client == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(client).build();
  }

  @POST
  @Transactional
  public Response create(Client client) {
    client.id = null;
    client.persist();
    return Response.status(Response.Status.CREATED).entity(client).build();
  }

  @PUT
  @Path("/{id}")
  @Transactional
  public Response update(@PathParam("id") Long id, Client updated) {
    Client client = Client.findById(id);
    if (client == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    client.name = updated.name;
    client.email = updated.email;
    return Response.ok(client).build();
  }

  @DELETE
  @Path("/{id}")
  @Transactional
  public Response delete(@PathParam("id") Long id) {
    boolean deleted = Client.deleteById(id);
    if (!deleted) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.noContent().build();
  }
}
