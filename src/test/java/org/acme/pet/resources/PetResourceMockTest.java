package org.acme.pet.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;
import org.acme.pet.resources.dtos.CategoryResponse;
import org.acme.pet.resources.dtos.PetResponse;
import org.acme.pet.resources.dtos.TagResponse;
import org.acme.pet.services.PetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Demonstrates {@code @InjectMocks} from Mockito.
 *
 * <p>Unlike the manual {@code new PetResource(service)} construction in {@link PetResourceTest},
 * {@code @InjectMocks} asks Mockito to instantiate the class under test and inject all
 * {@code @Mock} fields into it automatically via constructor, setter, or field injection. This is
 * useful when the class has multiple dependencies or when you want to avoid coupling the test setup
 * to the constructor signature.
 *
 * <p>Use {@code @InjectMocks} when the class under test has several collaborators. Use manual
 * construction (as in {@link PetResourceTest}) when you want an explicit, readable setup.
 */
@ExtendWith(MockitoExtension.class)
class PetResourceMockTest {

  @Mock PetService petService;

  @InjectMocks PetResource petResource;

  private static PetResponse pet(long id, String name, String status) {
    return new PetResponse(
        id,
        new CategoryResponse(10L, "dogs"),
        name,
        List.of("https://example.com/" + name + ".jpg"),
        List.of(new TagResponse(20L, "friendly")),
        status);
  }

  @Test
  void shouldReturnPetWhenFound() {
    PetResponse expected = pet(1L, "inject-dog", "available");
    when(petService.getById(1L)).thenReturn(Optional.of(expected));

    assertThat(petResource.getById(1L)).isEqualTo(expected);
    verify(petService).getById(1L);
  }

  @Test
  void shouldThrowNotFoundWhenPetIsMissing() {
    when(petService.getById(42L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> petResource.getById(42L))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("42");
  }

  @Test
  void shouldReturnPetsByStatus() {
    PetResponse available = pet(1L, "inject-dog", "available");
    when(petService.findByStatus(List.of("available"), null, null, null, null))
        .thenReturn(List.of(available));

    var result = petResource.findByStatus(List.of("available"), null, null, null, null);

    assertThat(result.items()).containsExactly(available);
  }

  @Test
  void shouldWireUriInfoForPostCreation() {
    UriInfo uriInfo = org.mockito.Mockito.mock(UriInfo.class);
    when(uriInfo.getAbsolutePathBuilder())
        .thenAnswer(inv -> UriBuilder.fromUri("http://localhost/pet"));
    petResource.uriInfo = uriInfo;

    PetResponse created = pet(7L, "new-dog", "available");
    org.acme.pet.resources.dtos.PetRequest request =
        new org.acme.pet.resources.dtos.PetRequest(
            null,
            null,
            "new-dog",
            List.of("https://example.com/new-dog.jpg"),
            List.of(),
            "available");
    when(petService.add(request)).thenReturn(created);

    var response = petResource.add(request);

    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeaderString("Location")).isEqualTo("http://localhost/pet/7");
  }

  /**
   * Demonstrates {@code @ParameterizedTest} with {@code @ValueSource} for the pet domain.
   *
   * <p>Running the same assertion across multiple inputs without duplicating test methods. Each
   * value in {@code @ValueSource} becomes a separate test case in the report.
   */
  @ParameterizedTest
  @ValueSource(longs = {100L, 200L, 300L})
  void shouldThrowNotFoundForUnknownPetIds(long petId) {
    when(petService.getById(petId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> petResource.getById(petId))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(String.valueOf(petId));
  }
}
