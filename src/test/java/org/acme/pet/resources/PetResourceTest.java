package org.acme.pet.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;
import org.acme.pet.resources.dtos.CategoryResponse;
import org.acme.pet.resources.dtos.PetRequest;
import org.acme.pet.resources.dtos.PetResponse;
import org.acme.pet.resources.dtos.TagRequest;
import org.acme.pet.resources.dtos.TagResponse;
import org.acme.pet.services.PetService;
import org.acme.shared.ApiResponse;
import org.acme.shared.ListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PetResourceTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Mock PetService service;

  private PetResource resource;

  @BeforeEach
  void setUp() {
    resource = new PetResource(service);
  }

  @Test
  void shouldCreateAndQueryPetLifecycle() {
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getAbsolutePathBuilder())
        .thenAnswer(invocation -> UriBuilder.fromUri("http://localhost/pet"));
    resource.uriInfo = uriInfo;

    long petId = 99L;
    PetRequest createRequest =
        new PetRequest(
            null,
            null,
            "doggie-plus",
            List.of("https://example.com/doggie-plus.jpg"),
            List.of(new TagRequest(20L, "friendly")),
            "available");
    PetResponse createdPet =
        new PetResponse(
            petId,
            new CategoryResponse(10L, "dogs"),
            "doggie-plus",
            List.of("https://example.com/doggie-plus.jpg"),
            List.of(new TagResponse(20L, "friendly")),
            "available");
    PetResponse updatedPet =
        new PetResponse(
            petId,
            new CategoryResponse(10L, "dogs"),
            "doggie-updated",
            List.of("https://example.com/doggie-updated.jpg"),
            List.of(new TagResponse(20L, "friendly")),
            "pending");
    PetResponse formUpdatedPet =
        new PetResponse(
            petId,
            new CategoryResponse(10L, "dogs"),
            "doggie-form",
            List.of("https://example.com/doggie-updated.jpg"),
            List.of(new TagResponse(20L, "friendly")),
            "sold");

    when(service.add(createRequest)).thenReturn(createdPet);
    when(service.getById(petId)).thenReturn(Optional.of(createdPet), Optional.empty());
    when(service.findByStatus(List.of("available"), null, null, null, null))
        .thenReturn(List.of(createdPet));
    when(service.findByTags(List.of("friendly"), null, null, null, null))
        .thenReturn(List.of(createdPet));
    when(service.update(createRequest)).thenReturn(updatedPet);
    when(service.updateWithForm(petId, "doggie-form", "sold"))
        .thenReturn(Optional.of(formUpdatedPet));
    when(service.uploadImage(petId, "cover", "pet.png"))
        .thenReturn(new ApiResponse(200, "success", "mock upload"));
    when(service.delete(petId)).thenReturn(true);

    Response createResponse = resource.add(createRequest);
    assertThat(createResponse.getStatus()).isEqualTo(201);
    assertThat(createResponse.getHeaderString("Location")).isEqualTo("http://localhost/pet/99");
    assertThat(createResponse.getEntity()).isEqualTo(createdPet);

    assertThat(resource.getById(petId)).isEqualTo(createdPet);

    ListResponse<PetResponse> byStatus =
        resource.findByStatus(List.of("available"), null, null, null, null);
    assertThat(byStatus.items()).containsExactly(createdPet);

    ListResponse<PetResponse> byTags =
        resource.findByTags(List.of("friendly"), null, null, null, null);
    assertThat(byTags.items()).containsExactly(createdPet);

    assertThat(resource.update(createRequest)).isEqualTo(updatedPet);

    Response updateWithFormResponse = resource.updateWithForm(petId, "doggie-form", "sold");
    assertThat(updateWithFormResponse.getStatus()).isEqualTo(200);
    assertThat(updateWithFormResponse.getEntity()).isEqualTo(formUpdatedPet);

    Response uploadResponse = resource.uploadImage(petId, "cover", "pet.png");
    assertThat(uploadResponse.getStatus()).isEqualTo(200);
    assertThat(uploadResponse.getEntity())
        .isEqualTo(new ApiResponse(200, "success", "mock upload"));

    Response deleteResponse = resource.delete("special-key", petId);
    assertThat(deleteResponse.getStatus()).isEqualTo(204);

    assertThatThrownBy(() -> resource.getById(petId))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Pet not found: 99");

    verify(service).add(createRequest);
    verify(service, org.mockito.Mockito.times(2)).getById(petId);
    verify(service).findByStatus(List.of("available"), null, null, null, null);
    verify(service).findByTags(List.of("friendly"), null, null, null, null);
    verify(service).update(createRequest);
    verify(service).updateWithForm(petId, "doggie-form", "sold");
    verify(service).uploadImage(petId, "cover", "pet.png");
    verify(service).delete(petId);
  }

  @Test
  void shouldThrowWhenPetIsMissing() {
    when(service.delete(999L)).thenReturn(false);
    when(service.updateWithForm(999L, "name", "sold")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> resource.delete("key", 999L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Pet not found: 999");
    assertThatThrownBy(() -> resource.updateWithForm(999L, "name", "sold"))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Pet not found: 999");
  }

  @Test
  void shouldValidatePetRequestWithoutHttpLayer() {
    PetRequest invalidRequest = new PetRequest(null, null, "", List.of(), List.of(), "invalid");

    var violations = validator.validate(invalidRequest);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString())
        .contains("name", "photoUrls", "status");
  }
}
