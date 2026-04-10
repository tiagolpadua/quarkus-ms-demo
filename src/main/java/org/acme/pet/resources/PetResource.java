package org.acme.pet.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.acme.pet.resources.dtos.PetRequest;
import org.acme.pet.resources.dtos.PetResponse;
import org.acme.pet.services.PetService;
import org.acme.shared.ApiResponse;
import org.acme.shared.ListResponse;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/pet")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Pet", description = "Operations for pet registration and lookup")
public class PetResource {

  private final PetService service;

  @Context UriInfo uriInfo;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Add a new pet")
  @RequestBody(
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = PetRequest.class)))
  @APIResponse(
      responseCode = "201",
      description = "Pet created",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = PetResponse.class)))
  @APIResponse(responseCode = "400", description = "Invalid request payload")
  public Response add(@Valid PetRequest pet) {
    PetResponse response = service.add(pet);
    return Response.created(
            uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build())
        .entity(response)
        .build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Update an existing pet")
  @RequestBody(
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = PetRequest.class)))
  @APIResponse(
      responseCode = "200",
      description = "Pet updated",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = PetResponse.class)))
  @APIResponse(responseCode = "404", description = "Pet not found")
  public PetResponse update(@Valid PetRequest pet) {
    return service.update(pet);
  }

  @GET
  @Path("/findByStatus")
  @Operation(summary = "Find pets by status")
  @APIResponse(responseCode = "200", description = "Filtered pet list")
  public ListResponse<PetResponse> findByStatus(
      @Parameter(
              name = "status",
              in = ParameterIn.QUERY,
              description = "Pet status values (available, pending, sold)")
          @QueryParam("status")
          List<String> statuses,
      @QueryParam("page") Integer page,
      @QueryParam("size") Integer size,
      @QueryParam("sort") String sortBy,
      @QueryParam("direction") String direction) {
    return new ListResponse<>(service.findByStatus(statuses, page, size, sortBy, direction));
  }

  @GET
  @Path("/findByTags")
  @Operation(summary = "Find pets by tags")
  @APIResponse(responseCode = "200", description = "Filtered pet list")
  public ListResponse<PetResponse> findByTags(
      @Parameter(
              name = "tags",
              in = ParameterIn.QUERY,
              description = "Tag names used for filtering")
          @QueryParam("tags")
          List<String> tags,
      @QueryParam("page") Integer page,
      @QueryParam("size") Integer size,
      @QueryParam("sort") String sortBy,
      @QueryParam("direction") String direction) {
    return new ListResponse<>(service.findByTags(tags, page, size, sortBy, direction));
  }

  @GET
  @Path("/{petId}")
  @Operation(summary = "Find pet by id")
  @APIResponse(
      responseCode = "200",
      description = "Pet found",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = PetResponse.class)))
  @APIResponse(responseCode = "404", description = "Pet not found")
  public PetResponse getById(@PathParam("petId") @Positive Long petId) {
    return service
        .getById(petId)
        .orElseThrow(() -> new NotFoundException("Pet not found: " + petId));
  }

  @POST
  @Path("/{petId}")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Operation(summary = "Update pet fields using form data")
  @APIResponse(
      responseCode = "200",
      description = "Pet updated",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = PetResponse.class)))
  @APIResponse(responseCode = "404", description = "Pet not found")
  public Response updateWithForm(
      @PathParam("petId") Long petId,
      @FormParam("name") String name,
      @FormParam("status") String status) {
    PetResponse response =
        service
            .updateWithForm(petId, name, status)
            .orElseThrow(() -> new NotFoundException("Pet not found: " + petId));
    return Response.ok(response).build();
  }

  @DELETE
  @Path("/{petId}")
  @Operation(summary = "Delete a pet")
  @APIResponse(responseCode = "204", description = "Pet deleted")
  @APIResponse(responseCode = "404", description = "Pet not found")
  public Response delete(
      @HeaderParam("api_key") String apiKey, @PathParam("petId") @Positive Long petId) {
    if (!service.delete(petId)) {
      throw new NotFoundException("Pet not found: " + petId);
    }
    return Response.noContent().build();
  }

  @POST
  @Path("/{petId}/uploadImage")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Operation(summary = "Upload image metadata for a pet")
  @APIResponse(
      responseCode = "200",
      description = "Image metadata accepted",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = ApiResponse.class)))
  public Response uploadImage(
      @PathParam("petId") Long petId,
      @FormParam("additionalMetadata") String additionalMetadata,
      @FormParam("file") String file) {
    ApiResponse response = service.uploadImage(petId, additionalMetadata, file);
    return Response.status(response.code()).entity(response).build();
  }
}
