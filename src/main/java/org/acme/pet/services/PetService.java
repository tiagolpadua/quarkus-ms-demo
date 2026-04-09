package org.acme.pet.services;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
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
  @WithSpan("PetService.add")
  public PetResponse add(PetRequest petRequest) {
    Pet createdPet = repository.save(normalize(mapper.toEntity(petRequest)));
    meterRegistry.counter("pet_create_total").increment();
    return mapper.toResponse(createdPet);
  }

  @Transactional
  @WithSpan("PetService.update")
  public PetResponse update(PetRequest petRequest) {
    return mapper.toResponse(repository.save(normalize(mapper.toEntity(petRequest))));
  }

  @WithSpan("PetService.findByStatus")
  public List<PetResponse> findByStatus(
      @SpanAttribute("arg.statuses") List<String> statuses,
      @SpanAttribute("arg.page") Integer page,
      @SpanAttribute("arg.size") Integer size,
      @SpanAttribute("arg.sortBy") String sortBy,
      @SpanAttribute("arg.direction") String direction) {
    return mapper.toResponseList(
        repository.findByStatus(
            statuses, normalizePage(page), normalizeSize(size), sortBy, direction));
  }

  @WithSpan("PetService.findByTags")
  public List<PetResponse> findByTags(
      @SpanAttribute("arg.tags") List<String> tags,
      @SpanAttribute("arg.page") Integer page,
      @SpanAttribute("arg.size") Integer size,
      @SpanAttribute("arg.sortBy") String sortBy,
      @SpanAttribute("arg.direction") String direction) {
    return mapper.toResponseList(
        repository.findByTags(tags, normalizePage(page), normalizeSize(size), sortBy, direction));
  }

  @WithSpan("PetService.getById")
  public Optional<PetResponse> getById(@SpanAttribute("arg.petId") Long petId) {
    return repository.findOptionalById(petId).map(mapper::toResponse);
  }

  @Transactional
  @WithSpan("PetService.updateWithForm")
  public Optional<PetResponse> updateWithForm(
      @SpanAttribute("arg.petId") Long petId,
      @SpanAttribute("arg.name") String name,
      @SpanAttribute("arg.status") String status) {
    return repository.updatePartial(petId, name, status).map(mapper::toResponse);
  }

  @Transactional
  @WithSpan("PetService.delete")
  public boolean delete(@SpanAttribute("arg.petId") Long petId) {
    return repository.delete(petId);
  }

  @WithSpan("PetService.uploadImage")
  public ApiResponse uploadImage(
      @SpanAttribute("arg.petId") Long petId,
      @SpanAttribute("arg.additionalMetadata") String additionalMetadata,
      @SpanAttribute("arg.fileName") String fileName) {
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
