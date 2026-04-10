package org.acme.user.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;
import org.acme.shared.ListResponse;
import org.acme.shared.pagination.PageMetadata;
import org.acme.shared.pagination.PagedResponse;
import org.acme.shared.pagination.SortMetadata;
import org.acme.user.resources.dtos.UserRequest;
import org.acme.user.resources.dtos.UserResponse;
import org.acme.user.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserResourceTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Mock UserService service;

  private UserResource resource;

  @BeforeEach
  void setUp() {
    resource = new UserResource(service);
  }

  @Test
  void shouldHandleUserOperationsWithDtosOnly() {
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getAbsolutePathBuilder())
        .thenAnswer(invocation -> UriBuilder.fromUri("http://localhost/user"));
    resource.uriInfo = uriInfo;

    UserRequest createRequest =
        new UserRequest(null, "user1", "User", "One", "user1@example.com", "+55 11 99999-1111", 1);
    UserResponse seedUser1 =
        new UserResponse(1L, "seed-user-1", "Seed", "One", "seed1@example.com", "555-0001", 1);
    UserResponse seedUser2 =
        new UserResponse(2L, "seed-user-2", "Seed", "Two", "seed2@example.com", "555-0002", 1);
    UserResponse createdUser =
        new UserResponse(3L, "user1", "User", "One", "user1@example.com", "+55 11 99999-1111", 1);
    UserResponse updatedUser =
        new UserResponse(
            3L, "user1", "Updated", "One", "updated.user1@example.com", "+55 11 99999-2222", 2);

    when(service.list(0, 20, "username", "asc")).thenReturn(List.of(seedUser1, seedUser2));
    when(service.create(createRequest)).thenReturn(createdUser);
    when(service.getByUsername("user1"))
        .thenReturn(Optional.of(createdUser))
        .thenReturn(Optional.empty());
    when(service.listByStatusNamedQuery(1)).thenReturn(List.of(seedUser1, seedUser2, createdUser));
    when(service.listByEmailDomainNativeQuery("example.com"))
        .thenReturn(List.of(seedUser1, seedUser2, createdUser));
    when(service.listByCriteria("seed-user", 1, "example.com"))
        .thenReturn(List.of(seedUser1, seedUser2));
    when(service.update("user1", createRequest)).thenReturn(Optional.of(updatedUser));
    when(service.createMany(List.of(createRequest))).thenReturn(List.of(createdUser));
    when(service.delete("user1")).thenReturn(true);

    assertThat(resource.list(0, 20, "username", "asc").items())
        .containsExactly(seedUser1, seedUser2);

    Response createResponse = resource.create(createRequest);
    assertThat(createResponse.getStatus()).isEqualTo(201);
    assertThat(createResponse.getHeaderString("Location")).isEqualTo("http://localhost/user/user1");
    assertThat(createResponse.getEntity()).isEqualTo(createdUser);

    assertThat(resource.getByUsername("user1")).isEqualTo(createdUser);
    assertThat(resource.listByNamedQuery(1).items())
        .containsExactly(seedUser1, seedUser2, createdUser);
    assertThat(resource.listByNamedNativeQuery("example.com").items())
        .containsExactly(seedUser1, seedUser2, createdUser);
    assertThat(resource.listByCriteria("seed-user", 1, "example.com").items())
        .containsExactly(seedUser1, seedUser2);

    Response updateResponse = resource.update("user1", createRequest);
    assertThat(updateResponse.getStatus()).isEqualTo(200);
    assertThat(updateResponse.getEntity()).isEqualTo(updatedUser);

    Response createWithArrayResponse = resource.createWithArray(List.of(createRequest));
    assertThat(createWithArrayResponse.getStatus()).isEqualTo(201);
    assertThat(createWithArrayResponse.getEntity())
        .isEqualTo(new ListResponse<>(List.of(createdUser)));

    Response createWithListResponse = resource.createWithList(List.of(createRequest));
    assertThat(createWithListResponse.getStatus()).isEqualTo(201);
    assertThat(createWithListResponse.getEntity())
        .isEqualTo(new ListResponse<>(List.of(createdUser)));

    Response deleteResponse = resource.delete("user1");
    assertThat(deleteResponse.getStatus()).isEqualTo(204);

    assertThatThrownBy(() -> resource.getByUsername("user1"))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("User not found: user1");

    verify(service).list(0, 20, "username", "asc");
    verify(service).create(createRequest);
    verify(service, org.mockito.Mockito.times(2)).getByUsername("user1");
    verify(service).listByStatusNamedQuery(1);
    verify(service).listByEmailDomainNativeQuery("example.com");
    verify(service).listByCriteria("seed-user", 1, "example.com");
    verify(service).update("user1", createRequest);
    verify(service, org.mockito.Mockito.times(2)).createMany(List.of(createRequest));
    verify(service).delete("user1");
  }

  @Test
  void shouldReturnPagedResponseWithNavigationLinks() {
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getAbsolutePathBuilder())
        .thenAnswer(invocation -> UriBuilder.fromUri("http://localhost/user/paged"));
    resource.uriInfo = uriInfo;

    PagedResponse<UserResponse> firstPage =
        new PagedResponse<>(
            List.of(
                new UserResponse(
                    2L, "seed-user-2", "Seed", "Two", "seed2@example.com", "555-0002", 1)),
            new PageMetadata(0, 1, 4, 4, true, false, true, false),
            new SortMetadata("username", "desc"));
    when(service.listPaged(0, 1, "username", "desc")).thenReturn(firstPage);

    Response response = resource.listPaged(0, 1, "username", "desc");

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getEntity()).isEqualTo(firstPage);
    assertThat(response.getHeaderString("X-Total-Count")).isEqualTo("4");
    assertThat(response.getLinks()).extracting(Link::getRel).contains("self", "next");
  }

  @Test
  void shouldValidateUserRequestWithoutHttpLayer() {
    UserRequest invalidRequest = new UserRequest(null, "ab", "", "", "invalid-email", "x", 99);

    var violations = validator.validate(invalidRequest);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString())
        .contains("username", "firstName", "lastName", "email", "phone", "userStatus");
  }

  @Test
  void shouldReturnPagedResponseWithNoPrevLinkOnFirstPage() {
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getAbsolutePathBuilder())
        .thenAnswer(invocation -> UriBuilder.fromUri("http://localhost/user/paged"));
    resource.uriInfo = uriInfo;

    PagedResponse<UserResponse> singlePage =
        new PagedResponse<>(
            List.of(
                new UserResponse(
                    1L, "only-user", "Only", "User", "only@example.com", "555-0001", 1)),
            new PageMetadata(0, 20, 1, 1, true, true, false, false),
            new SortMetadata("username", "asc"));
    when(service.listPaged(0, 20, "username", "asc")).thenReturn(singlePage);

    Response response = resource.listPaged(0, 20, "username", "asc");

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getLinks()).extracting(Link::getRel).contains("self");
    assertThat(response.getLinks()).extracting(Link::getRel).doesNotContain("next", "prev");
  }

  @Test
  void shouldReturnPagedResponseWithPrevLinkOnLastPage() {
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getAbsolutePathBuilder())
        .thenAnswer(invocation -> UriBuilder.fromUri("http://localhost/user/paged"));
    resource.uriInfo = uriInfo;

    PagedResponse<UserResponse> lastPage =
        new PagedResponse<>(
            List.of(
                new UserResponse(
                    5L, "last-user", "Last", "User", "last@example.com", "555-0005", 1)),
            new PageMetadata(2, 2, 5, 3, false, true, false, true),
            new SortMetadata("username", "asc"));
    when(service.listPaged(2, 2, "username", "asc")).thenReturn(lastPage);

    Response response = resource.listPaged(2, 2, "username", "asc");

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getLinks()).extracting(Link::getRel).contains("self", "prev");
    assertThat(response.getLinks()).extracting(Link::getRel).doesNotContain("next");
  }

  @Test
  void shouldThrowWhenUserIsMissing() {
    UserRequest updateRequest =
        new UserRequest(
            null, "missing", "Missing", "User", "missing@example.com", "+55 11 99999-1111", 1);
    when(service.update("missing", updateRequest)).thenReturn(Optional.empty());
    when(service.delete("missing")).thenReturn(false);

    assertThatThrownBy(() -> resource.update("missing", updateRequest))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("User not found: missing");
    assertThatThrownBy(() -> resource.delete("missing"))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("User not found: missing");
  }
}
