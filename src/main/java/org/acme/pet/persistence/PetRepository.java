package org.acme.pet.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.acme.pet.dtos.CategoryData;
import org.acme.pet.dtos.TagData;

@ApplicationScoped
public class PetRepository {

  private final Map<Long, Pet> pets = new LinkedHashMap<>();
  private final AtomicLong sequence = new AtomicLong();

  public PetRepository() {
    save(seedPet("doggie", "available", "dogs", "friendly"));
    save(seedPet("catty", "pending", "cats", "indoor"));
  }

  public List<Pet> listAll() {
    return pets.values().stream().map(this::copyOf).collect(Collectors.toList());
  }

  public List<Pet> findByStatus(List<String> statuses) {
    return pets.values().stream()
        .filter(pet -> statuses.contains(pet.status()))
        .map(this::copyOf)
        .collect(Collectors.toList());
  }

  public List<Pet> findByTags(List<String> tagNames) {
    return pets.values().stream()
        .filter(
            pet ->
                pet.tags() != null
                    && pet.tags().stream().map(TagData::name).anyMatch(tagNames::contains))
        .map(this::copyOf)
        .collect(Collectors.toList());
  }

  public Optional<Pet> findById(Long id) {
    return Optional.ofNullable(pets.get(id)).map(this::copyOf);
  }

  public Pet save(Pet pet) {
    Pet copy = copyOf(pet);
    if (copy.id() == null) {
      copy =
          new Pet(
              sequence.incrementAndGet(),
              copy.category(),
              copy.name(),
              copy.photoUrls(),
              copy.tags(),
              copy.status());
    } else {
      sequence.accumulateAndGet(copy.id(), Math::max);
    }
    pets.put(copy.id(), copy);
    return copyOf(copy);
  }

  public Optional<Pet> updatePartial(Long petId, String name, String status) {
    Pet pet = pets.get(petId);
    if (pet == null) {
      return Optional.empty();
    }
    Pet updated =
        new Pet(
            pet.id(),
            pet.category(),
            name != null && !name.isBlank() ? name : pet.name(),
            pet.photoUrls(),
            pet.tags(),
            status != null && !status.isBlank() ? status : pet.status());
    pets.put(petId, updated);
    return Optional.of(copyOf(updated));
  }

  public boolean delete(Long petId) {
    return pets.remove(petId) != null;
  }

  private Pet seedPet(String name, String status, String categoryName, String tagName) {
    long id = sequence.incrementAndGet();
    return new Pet(
        id,
        new CategoryData(id, categoryName),
        name,
        new ArrayList<>(List.of("https://example.com/" + name + ".jpg")),
        new ArrayList<>(List.of(new TagData(id, tagName))),
        status);
  }

  private Pet copyOf(Pet source) {
    return new Pet(
        source.id(),
        source.category() == null
            ? null
            : new CategoryData(source.category().id(), source.category().name()),
        source.name(),
        source.photoUrls() == null ? new ArrayList<>() : new ArrayList<>(source.photoUrls()),
        source.tags() == null
            ? new ArrayList<>()
            : source.tags().stream()
                .map(t -> new TagData(t.id(), t.name()))
                .collect(Collectors.toList()),
        source.status());
  }
}
