package org.acme.pet.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.acme.pet.persistence.Pet;
import org.acme.pet.services.PetService;
import org.acme.shared.ApiResponse;

@Path("/pet")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class PetResource {

  private final PetService service;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Pet add(Pet pet) {
    return service.add(pet);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Pet update(Pet pet) {
    return service.update(pet);
  }

  @GET
  @Path("/findByStatus")
  public List<Pet> findByStatus(@QueryParam("status") List<String> statuses) {
    return service.findByStatus(statuses);
  }

  @GET
  @Path("/findByTags")
  public List<Pet> findByTags(@QueryParam("tags") List<String> tags) {
    return service.findByTags(tags);
  }

  @GET
  @Path("/{petId}")
  public Response getById(@PathParam("petId") Long petId) {
    return service
        .getById(petId)
        .map(pet -> Response.ok(pet).build())
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  @POST
  @Path("/{petId}")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response updateWithForm(
      @PathParam("petId") Long petId,
      @FormParam("name") String name,
      @FormParam("status") String status) {
    return service
        .updateWithForm(petId, name, status)
        .map(pet -> Response.ok(pet).build())
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  @DELETE
  @Path("/{petId}")
  public Response delete(@HeaderParam("api_key") String apiKey, @PathParam("petId") Long petId) {
    return service.delete(petId)
        ? Response.noContent().build()
        : Response.status(Response.Status.NOT_FOUND).build();
  }

  @POST
  @Path("/{petId}/uploadImage")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response uploadImage(
      @PathParam("petId") Long petId,
      @FormParam("additionalMetadata") String additionalMetadata,
      @FormParam("file") String file) {
    ApiResponse response = service.uploadImage(petId, additionalMetadata, file);
    return Response.status(response.code()).entity(response).build();
  }
}
