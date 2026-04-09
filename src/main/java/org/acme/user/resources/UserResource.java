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
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.acme.shared.ListResponse;
import org.acme.shared.pagination.PagedResponse;
import org.acme.user.resources.dtos.UserRequest;
import org.acme.user.resources.dtos.UserResponse;
import org.acme.user.services.UserService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "User", description = "Operations for user management")
public class UserResource {

  private final UserService service;

  @Context UriInfo uriInfo;

  @GET
  @Operation(summary = "List users")
  @APIResponse(responseCode = "200", description = "User list")
  public ListResponse<UserResponse> list(
      @QueryParam("page") @DefaultValue("0") @Min(0) int page,
      @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size,
      @QueryParam("sort") @DefaultValue("username") String sortBy,
      @QueryParam("direction") @DefaultValue("asc") String direction) {
    return new ListResponse<>(service.list(page, size, sortBy, direction));
  }

  @GET
  @Path("/paged")
  @Operation(summary = "List users with pagination metadata")
  @APIResponse(responseCode = "200", description = "Paged user list")
  public Response listPaged(
      @QueryParam("page") @DefaultValue("0") @Min(0) int page,
      @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size,
      @QueryParam("sort") @DefaultValue("username") String sortBy,
      @QueryParam("direction") @DefaultValue("asc") String direction) {
    PagedResponse<UserResponse> pagedResponse = service.listPaged(page, size, sortBy, direction);
    Response.ResponseBuilder responseBuilder =
        Response.ok(pagedResponse).header("X-Total-Count", pagedResponse.page().totalElements());
    addPaginationLinks(responseBuilder, pagedResponse);
    return responseBuilder.build();
  }

  @GET
  @Path("/examples/named-query")
  @Operation(summary = "Example query using @NamedQuery")
  @APIResponse(responseCode = "200", description = "User list")
  public ListResponse<UserResponse> listByNamedQuery(@QueryParam("status") Integer userStatus) {
    return new ListResponse<>(service.listByStatusNamedQuery(userStatus));
  }

  @GET
  @Path("/examples/named-native-query")
  @Operation(summary = "Example query using @NamedNativeQuery")
  @APIResponse(responseCode = "200", description = "User list")
  public ListResponse<UserResponse> listByNamedNativeQuery(
      @QueryParam("emailDomain") String emailDomain) {
    return new ListResponse<>(service.listByEmailDomainNativeQuery(emailDomain));
  }

  @GET
  @Path("/examples/criteria")
  @Operation(summary = "Example query using Criteria API")
  @APIResponse(responseCode = "200", description = "User list")
  public ListResponse<UserResponse> listByCriteria(
      @QueryParam("usernamePrefix") String usernamePrefix,
      @QueryParam("status") Integer userStatus,
      @QueryParam("emailDomain") String emailDomain) {
    return new ListResponse<>(service.listByCriteria(usernamePrefix, userStatus, emailDomain));
  }

  @POST
  @Operation(summary = "Create user")
  @RequestBody(
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = UserRequest.class)))
  @APIResponses({
    @APIResponse(
        responseCode = "201",
        description = "User created",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserResponse.class))),
    @APIResponse(responseCode = "400", description = "Invalid request payload")
  })
  public Response create(@Valid UserRequest user) {
    UserResponse response = service.create(user);
    return Response.created(uriInfo.getAbsolutePathBuilder().path(response.username()).build())
        .entity(response)
        .build();
  }

  @POST
  @Path("/createWithArray")
  @Operation(summary = "Create multiple users from array")
  @RequestBody(
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = UserRequest.class)))
  @APIResponse(responseCode = "201", description = "Users created")
  public Response createWithArray(@NotEmpty List<@Valid UserRequest> users) {
    return Response.status(Response.Status.CREATED)
        .entity(new ListResponse<>(service.createMany(users)))
        .build();
  }

  @POST
  @Path("/createWithList")
  @Operation(summary = "Create multiple users from list")
  @RequestBody(
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = UserRequest.class)))
  @APIResponse(responseCode = "201", description = "Users created")
  public Response createWithList(@NotEmpty List<@Valid UserRequest> users) {
    return Response.status(Response.Status.CREATED)
        .entity(new ListResponse<>(service.createMany(users)))
        .build();
  }

  @GET
  @Path("/{username}")
  @Operation(summary = "Get user by username")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "User found",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserResponse.class))),
    @APIResponse(responseCode = "404", description = "User not found")
  })
  public UserResponse getByUsername(@PathParam("username") @NotBlank String username) {
    return service
        .getByUsername(username)
        .orElseThrow(() -> new NotFoundException("User not found: " + username));
  }

  @PUT
  @Path("/{username}")
  @Operation(summary = "Update user by username")
  @RequestBody(
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = UserRequest.class)))
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "User updated",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UserResponse.class))),
    @APIResponse(responseCode = "404", description = "User not found")
  })
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
  @Operation(summary = "Delete user by username")
  @APIResponses({
    @APIResponse(responseCode = "204", description = "User deleted"),
    @APIResponse(responseCode = "404", description = "User not found")
  })
  public Response delete(@PathParam("username") @NotBlank String username) {
    if (!service.delete(username)) {
      throw new NotFoundException("User not found: " + username);
    }
    return Response.noContent().build();
  }

  private void addPaginationLinks(
      Response.ResponseBuilder responseBuilder, PagedResponse<UserResponse> pagedResponse) {
    PageLinks pageLinks = buildPageLinks(pagedResponse);
    responseBuilder.links(pageLinks.self());
    if (pageLinks.next() != null) {
      responseBuilder.links(pageLinks.next());
    }
    if (pageLinks.prev() != null) {
      responseBuilder.links(pageLinks.prev());
    }
  }

  private PageLinks buildPageLinks(PagedResponse<UserResponse> pagedResponse) {
    int pageNumber = pagedResponse.page().number();
    int pageSize = pagedResponse.page().size();
    String sortBy = pagedResponse.sort().by();
    String direction = pagedResponse.sort().direction();

    Link self = buildPageLink(pageNumber, pageSize, sortBy, direction, "self");
    Link next =
        pagedResponse.page().hasNext()
            ? buildPageLink(pageNumber + 1, pageSize, sortBy, direction, "next")
            : null;
    Link prev =
        pagedResponse.page().hasPrevious()
            ? buildPageLink(pageNumber - 1, pageSize, sortBy, direction, "prev")
            : null;
    return new PageLinks(self, next, prev);
  }

  private Link buildPageLink(int page, int size, String sortBy, String direction, String rel) {
    return Link.fromUri(
            uriInfo
                .getAbsolutePathBuilder()
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sortBy)
                .queryParam("direction", direction)
                .build())
        .rel(rel)
        .type(MediaType.APPLICATION_JSON)
        .build();
  }

  private record PageLinks(Link self, Link next, Link prev) {}
}
