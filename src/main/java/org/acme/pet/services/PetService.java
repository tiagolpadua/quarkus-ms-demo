package org.acme.pet.services;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.acme.pet.persistence.Pet;
import org.acme.pet.persistence.PetRepository;
import org.acme.pet.resources.dtos.PetRequest;
import org.acme.pet.resources.dtos.PetResponse;
import org.acme.pet.services.mappers.PetMapper;
import org.acme.shared.ApiResponse;

@ApplicationScoped
@RequiredArgsConstructor
public class PetService {

  private final PetRepository repository;
  private final MeterRegistry meterRegistry;
  private final PetMapper mapper;

  @Transactional
  public PetResponse add(PetRequest petRequest) {
    Pet createdPet = repository.save(normalize(mapper.toEntity(petRequest)));
    meterRegistry.counter("pet_create_total").increment();
    return mapper.toResponse(createdPet);
  }

  @Transactional
  public PetResponse update(PetRequest petRequest) {
    return mapper.toResponse(repository.save(normalize(mapper.toEntity(petRequest))));
  }

  public List<PetResponse> findByStatus(
      List<String> statuses, Integer page, Integer size, String sortBy, String direction) {
    return mapper.toResponseList(
        repository.findByStatus(
            statuses, normalizePage(page), normalizeSize(size), sortBy, direction));
  }

  public List<PetResponse> findByTags(
      List<String> tags, Integer page, Integer size, String sortBy, String direction) {
    return mapper.toResponseList(
        repository.findByTags(tags, normalizePage(page), normalizeSize(size), sortBy, direction));
  }

  public Optional<PetResponse> getById(Long petId) {
    return repository.findOptionalById(petId).map(mapper::toResponse);
  }

  @Transactional
  public Optional<PetResponse> updateWithForm(Long petId, String name, String status) {
    return repository.updatePartial(petId, name, status).map(mapper::toResponse);
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
