package org.acme.pet.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.acme.pet.dtos.ApiResponseData;
import org.acme.pet.persistence.Pet;
import org.acme.pet.persistence.PetRepository;

@ApplicationScoped
public class PetService {

  @Inject PetRepository repository;

  public Pet add(Pet pet) {
    return repository.save(normalize(pet));
  }

  public Pet update(Pet pet) {
    return repository.save(normalize(pet));
  }

  public List<Pet> findByStatus(List<String> statuses) {
    return repository.findByStatus(statuses);
  }

  public List<Pet> findByTags(List<String> tags) {
    return repository.findByTags(tags);
  }

  public Optional<Pet> getById(Long petId) {
    return repository.findById(petId);
  }

  public Optional<Pet> updateWithForm(Long petId, String name, String status) {
    return repository.updatePartial(petId, name, status);
  }

  public boolean delete(Long petId) {
    return repository.delete(petId);
  }

  public ApiResponseData uploadImage(Long petId, String additionalMetadata, String fileName) {
    int code = repository.findById(petId).isPresent() ? 200 : 404;
    return new ApiResponseData(
        code,
        code == 200 ? "success" : "error",
        code == 200
            ? "Image uploaded for pet " + petId + formatDetails(additionalMetadata, fileName)
            : "Pet not found");
  }

  private Pet normalize(Pet pet) {
    return new Pet(
        pet.id(),
        pet.category(),
        pet.name(),
        pet.photoUrls() == null ? new ArrayList<>() : new ArrayList<>(pet.photoUrls()),
        pet.tags() == null ? new ArrayList<>() : new ArrayList<>(pet.tags()),
        pet.status());
  }

  private String formatDetails(String additionalMetadata, String fileName) {
    List<String> details = new ArrayList<>();
    if (additionalMetadata != null && !additionalMetadata.isBlank()) {
      details.add("metadata=" + additionalMetadata);
    }
    if (fileName != null && !fileName.isBlank()) {
      details.add("file=" + fileName);
    }
    return details.isEmpty() ? "" : " (" + String.join(", ", details) + ")";
  }
}
