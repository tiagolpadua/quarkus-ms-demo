package org.acme.pet.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
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
import org.acme.pet.dtos.PetRequest;
import org.acme.pet.dtos.PetResponse;
import org.acme.pet.mappers.PetMapper;
import org.acme.pet.services.PetService;
import org.acme.shared.ApiResponse;

@Path("/pet")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class PetResource {

  private final PetService service;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public PetResponse add(PetRequest pet) {
    return PetMapper.toResponse(service.add(PetMapper.toEntity(pet)));
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public PetResponse update(PetRequest pet) {
    return PetMapper.toResponse(service.update(PetMapper.toEntity(pet)));
  }

  @GET
  @Path("/findByStatus")
  public List<PetResponse> findByStatus(@QueryParam("status") List<String> statuses) {
    return PetMapper.toResponseList(service.findByStatus(statuses));
  }

  @GET
  @Path("/findByTags")
  public List<PetResponse> findByTags(@QueryParam("tags") List<String> tags) {
    return PetMapper.toResponseList(service.findByTags(tags));
  }

  @GET
  @Path("/{petId}")
  public PetResponse getById(@PathParam("petId") Long petId) {
    return service
        .getById(petId)
        .map(PetMapper::toResponse)
        .orElseThrow(() -> new NotFoundException("Pet not found: " + petId));
  }

  @POST
  @Path("/{petId}")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response updateWithForm(
      @PathParam("petId") Long petId,
      @FormParam("name") String name,
      @FormParam("status") String status) {
    PetResponse response =
        service
            .updateWithForm(petId, name, status)
            .map(PetMapper::toResponse)
            .orElseThrow(() -> new NotFoundException("Pet not found: " + petId));
    return Response.ok(response).build();
  }

  @DELETE
  @Path("/{petId}")
  public Response delete(@HeaderParam("api_key") String apiKey, @PathParam("petId") Long petId) {
    if (!service.delete(petId)) {
      throw new NotFoundException("Pet not found: " + petId);
    }
    return Response.noContent().build();
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
