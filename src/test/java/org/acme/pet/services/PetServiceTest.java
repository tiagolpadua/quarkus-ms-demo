package org.acme.pet.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Optional;
import org.acme.pet.persistence.Pet;
import org.acme.pet.persistence.PetRepository;
import org.acme.pet.resources.dtos.CategoryRequest;
import org.acme.pet.resources.dtos.PetRequest;
import org.acme.pet.resources.dtos.TagRequest;
import org.acme.pet.services.mappers.PetMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

  @Mock PetRepository repository;
  @Mock MeterRegistry meterRegistry;

  private PetService service;

  @BeforeEach
  void setUp() {
    PetMapper mapper = Mappers.getMapper(PetMapper.class);
    service = new PetService(repository, meterRegistry, mapper);
  }

  @Test
  void shouldCreateUpdateAndDeletePet() {
    Counter counter = mock(Counter.class);
    when(meterRegistry.counter("pet_create_total")).thenReturn(counter);
    when(repository.save(any(Pet.class)))
        .thenAnswer(
            invocation -> {
              Pet pet = invocation.getArgument(0);
              if (pet.getId() == null) {
                pet.setId(99L);
              }
              return pet;
            });
    when(repository.findOptionalById(99L))
        .thenReturn(Optional.of(pet(99L, "service-dog", "available")))
        .thenReturn(Optional.of(pet(99L, "service-dog", "available")))
        .thenReturn(Optional.empty())
        .thenReturn(Optional.empty());
    when(repository.updatePartial(99L, "service-dog-form", "sold"))
        .thenReturn(Optional.of(pet(99L, "service-dog-form", "sold")));
    when(repository.delete(99L)).thenReturn(true);

    PetRequest createRequest =
        new PetRequest(
            null,
            new CategoryRequest(10L, "dogs"),
            "service-dog",
            List.of("https://example.com/service-dog.jpg"),
            List.of(new TagRequest(20L, "friendly")),
            "available");

    var created = service.add(createRequest);

    assertThat(created.id()).isNotNull();
    assertThat(created.name()).isEqualTo("service-dog");
    assertThat(created.status()).isEqualTo("available");
    verify(counter).increment();

    assertThat(service.getById(created.id())).isPresent();

    PetRequest updateRequest =
        new PetRequest(
            created.id(),
            new CategoryRequest(10L, "dogs"),
            "service-dog-updated",
            List.of("https://example.com/service-dog-updated.jpg"),
            List.of(new TagRequest(20L, "friendly")),
            "pending");

    var updated = service.update(updateRequest);

    assertThat(updated.name()).isEqualTo("service-dog-updated");
    assertThat(updated.status()).isEqualTo("pending");

    var updatedWithForm = service.updateWithForm(created.id(), "service-dog-form", "sold");

    assertThat(updatedWithForm).isPresent();
    assertThat(updatedWithForm.orElseThrow().name()).isEqualTo("service-dog-form");
    assertThat(updatedWithForm.orElseThrow().status()).isEqualTo("sold");

    var uploadSuccess = service.uploadImage(created.id(), "cover", "pet.png");
    assertThat(uploadSuccess.code()).isEqualTo(200);
    assertThat(uploadSuccess.type()).isEqualTo("success");

    assertThat(service.delete(created.id())).isTrue();
    assertThat(service.getById(created.id())).isEmpty();

    var uploadMissingPet = service.uploadImage(created.id(), "cover", "pet.png");
    assertThat(uploadMissingPet.code()).isEqualTo(404);
    assertThat(uploadMissingPet.type()).isEqualTo("error");
  }

  /**
   * {@code @CsvSource} — each string is a comma-separated row of arguments.
   *
   * <p>Tests the same assertion for multiple (name, status) pairs without duplicating test methods.
   * Each row appears as a separate test case in the build report.
   */
  @ParameterizedTest
  @CsvSource({"buddy, available", "luna, pending", "max, sold"})
  void shouldFindPetByStatusForDifferentPets(String name, String status) {
    when(repository.findByStatus(List.of(status), 0, 20, "name", "asc"))
        .thenReturn(List.of(pet(1L, name, status)));

    var results = service.findByStatus(List.of(status), 0, 20, "name", "asc");

    assertThat(results).hasSize(1);
    assertThat(results.get(0).name()).isEqualTo(name);
    assertThat(results.get(0).status()).isEqualTo(status);
  }

  private Pet pet(Long id, String name, String status) {
    Pet pet = new Pet();
    pet.setId(id);
    pet.setCategory(new org.acme.pet.persistence.Category(10L, "dogs"));
    pet.setName(name);
    pet.setPhotoUrls(List.of("https://example.com/" + name + ".jpg"));
    pet.setTags(List.of(new org.acme.pet.persistence.Tag(20L, "friendly")));
    pet.setStatus(status);
    return pet;
  }
}
