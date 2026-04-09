package org.acme.pet.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.acme.pet.persistence.Pet;
import org.acme.pet.persistence.PetRepository;
import org.acme.shared.ApiResponse;

@ApplicationScoped
@RequiredArgsConstructor
public class PetService {

  private final PetRepository repository;

  @Transactional
  public Pet add(Pet pet) {
    return repository.save(normalize(pet));
  }

  @Transactional
  public Pet update(Pet pet) {
    return repository.save(normalize(pet));
  }

  public List<Pet> findByStatus(
      List<String> statuses, Integer page, Integer size, String sortBy, String direction) {
    return repository.findByStatus(
        statuses, normalizePage(page), normalizeSize(size), sortBy, direction);
  }

  public List<Pet> findByTags(
      List<String> tags, Integer page, Integer size, String sortBy, String direction) {
    return repository.findByTags(tags, normalizePage(page), normalizeSize(size), sortBy, direction);
  }

  public Optional<Pet> getById(Long petId) {
    return repository.findOptionalById(petId);
  }

  @Transactional
  public Optional<Pet> updateWithForm(Long petId, String name, String status) {
    return repository.updatePartial(petId, name, status);
  }

  @Transactional
  public boolean delete(Long petId) {
    return repository.delete(petId);
  }

  public ApiResponse uploadImage(Long petId, String additionalMetadata, String fileName) {
    int code = repository.findOptionalById(petId).isPresent() ? 200 : 404;
    return new ApiResponse(
        code,
        code == 200 ? "success" : "error",
        code == 200
            ? "Image uploaded for pet " + petId + formatDetails(additionalMetadata, fileName)
            : "Pet not found");
  }

  private Pet normalize(Pet pet) {
    Pet normalized = new Pet();
    normalized.setId(pet.getId());
    normalized.setCategory(pet.getCategory());
    normalized.setName(pet.getName());
    normalized.setStatus(pet.getStatus());
    normalized.setPhotoUrls(
        pet.getPhotoUrls() == null ? new ArrayList<>() : new ArrayList<>(pet.getPhotoUrls()));
    normalized.setTags(pet.getTags() == null ? new ArrayList<>() : new ArrayList<>(pet.getTags()));
    return normalized;
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

  private int normalizePage(Integer page) {
    return page == null ? 0 : Math.max(page, 0);
  }

  private int normalizeSize(Integer size) {
    return size == null ? 20 : Math.clamp(size, 1, 100);
  }
}
