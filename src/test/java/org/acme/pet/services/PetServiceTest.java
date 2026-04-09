package org.acme.pet.services;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.acme.pet.resources.dtos.CategoryRequest;
import org.acme.pet.resources.dtos.PetRequest;
import org.acme.pet.resources.dtos.TagRequest;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestTransaction
class PetServiceTest {

  @Inject PetService service;

  @Test
  void shouldCreateUpdateAndDeletePet() {
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
}
